package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.global.security.AuthContext;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderItem;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.event.OrderCancelledEvent;
import com.sparta.orderservice.global.port.CompanyPort;
import com.sparta.orderservice.order.application.port.DeliveryPort;
import com.sparta.orderservice.order.application.port.HubStockPort;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final CompanyOrderRepository companyOrderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final HubStockPort hubStockPort;
    private final CompanyPort companyPort;
    private final DeliveryPort deliveryPort;
    private final OrderWriter orderWriter;
    private final AuthContext authContext;

    /**
     * 주문 생성 — DB TX 없이 각 단계를 독립 TX로 분리
     *
     * 흐름:
     * (1) 도메인 객체 구성 (orderId는 Order.of() 내부에서 미리 생성)
     * (2) company-service 허브 매핑 조회 (외부 호출, TX 없음)
     * (3) hub-service 재고 예약 (외부 호출, TX 없음)
     * (4) delivery-service 배송 생성 (외부 호출, TX 없음)
     * (5) DB 저장 + 결제 이벤트 발행 (OrderWriter의 독립 TX)
     *     → 실패 시 (4)→(3) 순으로 Saga 보상 실행
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderResult createOrder(CreateOrderCommand command, UUID requesterId) {
        // COMPANY_MANAGER만 주문 생성 가능
        if (!authContext.isCompanyManager()) {
            throw new BusinessException(OrderErrorCode.FORBIDDEN);
        }
        if (command.receiverCompanyId() == null) {
            throw new BusinessException(OrderErrorCode.RECEIVER_COMPANY_REQUIRED);
        }

        // (1) 도메인 객체 구성 (TX 없음 — orderId는 Order.of() 내부에서 미리 생성됨)
        Order order = buildOrder(command);

        // (2) 수령업체 + 공급업체 ID 전체 → companyId : hubId 매핑 일괄 조회 (TX 없음)
        List<UUID> allCompanyIds = Stream.concat(
                Stream.of(order.getReceiverCompanyId()),
                order.getCompanyOrders().stream().map(CompanyOrder::getCompanyId)
        ).toList();
        Map<UUID, UUID> hubIdMap = companyPort.getHubIds(allCompanyIds);
        validateHubMapping(hubIdMap, allCompanyIds);
        UUID destinationHubId = hubIdMap.get(order.getReceiverCompanyId());

        // Saga 보상 스택: 외부 호출 성공 시 역순 보상 등록, 예외 발생 시 LIFO 순으로 실행
        Deque<Runnable> compensations = new ArrayDeque<>();
        try {
            // (3) 재고 예약 (TX 없음)
            // reserveStock: CompanyOrder 수만큼 루프 호출 → 중간 실패 시 부분 예약 잔존
            // → cancelStock으로 정리 (보상을 먼저 등록 후 호출)
            compensations.push(() -> hubStockPort.cancelStock(order.getOrderId()));
            hubStockPort.reserveStock(order);

            // (4) 배송 일괄 생성 (TX 없음) → 실패 시 (3) 보상
            Map<UUID, UUID> deliveryMap = deliveryPort.createDeliveries(order, hubIdMap, destinationHubId);
            List<UUID> companyOrderIds = order.getCompanyOrders().stream()
                    .map(CompanyOrder::getCompanyOrderId)
                    .toList();
            compensations.push(() -> deliveryPort.cancelDeliveries(companyOrderIds));

            // 배송 ID 할당 (OrderItem ↔ Delivery 추적용)
            order.getCompanyOrders().forEach(co -> {
                UUID deliveryId = deliveryMap.get(co.getCompanyOrderId());
                if (deliveryId != null) {
                    co.getOrderItems().forEach(item -> item.assignDelivery(deliveryId));
                }
            });

            // (5) DB 저장 + 선결제 (독립 TX) → 실패 시 (4)→(3) 순으로 보상
            // OrderCreatedEvent → PaymentEventHandler → createCompletedPayment (같은 TX)
            return orderWriter.saveOrderWithEvent(order);

        } catch (Exception e) {
            executeCompensations(compensations);
            throw e;
        }
    }

    // 주문 전체 취소
    @Transactional
    public void cancelOrder(UUID orderId, UUID requesterId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        // COMPANY_MANAGER는 자기 회사가 수령업체(주문 생성자)인 주문만 취소 가능
        if (authContext.isCompanyManager()) {
            if (!order.getReceiverCompanyId().equals(authContext.getCompanyId())) {
                throw new BusinessException(OrderErrorCode.FORBIDDEN);
            }
        } else if (!authContext.isMaster()) {
            throw new BusinessException(OrderErrorCode.FORBIDDEN);
        }

        validateOrderCancellable(order);

        // 이미 CANCELLED인 CompanyOrder는 건너뜀 (PENDING 상태면 SHIPPED/DELIVERED는 없음)
        order.getCompanyOrders().stream()
                .filter(co -> co.getStatus() != CompanyOrderStatus.CANCELLED)
                .forEach(co -> co.cancel(requesterId));
        order.cancel(requesterId);

        // Saga 보상 스택
        Deque<Runnable> compensations = new ArrayDeque<>();
        try {
            // (1) 재고 예약 전체 취소 (orderId 기준, 단일 호출)
            // 실패 시: TX 롤백으로 DB 복원, hub-service 미변경 → 보상 불필요 → push는 성공 후 등록
            hubStockPort.cancelStock(orderId);
            compensations.push(() -> hubStockPort.reserveStock(order));

            // (2) 결제 취소 이벤트 (PaymentEventHandler, 같은 트랜잭션)
            // 실패 시: T1 보상 (재고 재예약) 후 예외 re-throw → TX 롤백
            eventPublisher.publishEvent(new OrderCancelledEvent(orderId, requesterId));

        } catch (Exception e) {
            executeCompensations(compensations);
            throw e;
        }
    }

    // 서브 주문 부분 취소
    @Transactional
    public void cancelCompanyOrder(UUID companyOrderId, UUID requesterId) {
        CompanyOrder companyOrder = findCompanyOrderWithOrderAndSiblingsOrThrow(companyOrderId);

        // COMPANY_MANAGER는 자기 회사가 공급업체인 CompanyOrder만 취소 가능
        if (authContext.isCompanyManager()) {
            if (!companyOrder.getCompanyId().equals(authContext.getCompanyId())) {
                throw new BusinessException(OrderErrorCode.FORBIDDEN);
            }
        } else if (!authContext.isMaster()) {
            throw new BusinessException(OrderErrorCode.FORBIDDEN);
        }

        validateCompanyOrderCancellable(companyOrder);

        companyOrder.cancel(requesterId);

        // Saga 보상 스택
        Deque<Runnable> compensations = new ArrayDeque<>();
        try {
            // [T1] 재고 예약 부분 취소 (단일 호출, 원자적)
            // 실패 시: TX 롤백으로 DB 복원, hub-service 미변경 → 보상 불필요 → push는 성공 후 등록
            hubStockPort.cancelCompanyStock(companyOrderId);
            compensations.push(() -> hubStockPort.reserveCompanyStock(companyOrder));

            // 주문 상태 업데이트 (모든 서브 주문이 터미널 상태면 Order도 종료)
            Order order = companyOrder.getOrder();
            order.updateStatus(requesterId);

            // [T2] 마지막 서브 주문 취소 시 Order → CANCELLED → 결제 취소 이벤트 발행
            // 실패 시: T1 보상 (재고 재예약) 후 예외 re-throw → TX 롤백
            if (order.getStatus() == OrderStatus.CANCELLED) {
                eventPublisher.publishEvent(new OrderCancelledEvent(order.getOrderId(), requesterId));
            }
        } catch (Exception e) {
            executeCompensations(compensations);
            throw e;
        }
    }

    // Order + CompanyOrder + OrderItem 도메인 객체 구성
    private Order buildOrder(CreateOrderCommand command) {
        // CompanyOrder별 소계를 먼저 계산한 뒤 합산 → 아이템당 calculateItemPrice 호출 1회
        List<BigDecimal> subtotals = command.companyOrders().stream()
            .map(co -> co.orderItems().stream()
                .map(this::calculateItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .toList();
        BigDecimal totalPrice = subtotals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.of(
            command.receiverCompanyId(),
            command.recipientName(),
            command.phone(),
            command.slackId(),
            command.address(),
            command.dueDate(),
            command.requestMemo(),
            totalPrice,
            BigDecimal.ZERO,
            totalPrice          // finalPrice = totalPrice + deliveryFee (배송비 확정 전 임시)
        );

        List<CreateOrderCommand.CompanyOrderCommand> coCommands = command.companyOrders();
        for (int i = 0; i < coCommands.size(); i++) {
            order.addCompanyOrder(buildCompanyOrder(order, coCommands.get(i), subtotals.get(i)));
        }
        return order;
    }

    // CompanyOrder + OrderItem 도메인 객체 구성 (subtotal은 buildOrder에서 계산된 값 재사용)
    private CompanyOrder buildCompanyOrder(Order order, CreateOrderCommand.CompanyOrderCommand coCmd,
                                           BigDecimal subtotal) {
        CompanyOrder companyOrder = CompanyOrder.of(order, coCmd.companyId(), subtotal, BigDecimal.ZERO);
        coCmd.orderItems().forEach(itemCmd ->
            companyOrder.addOrderItem(
                OrderItem.of(companyOrder, itemCmd.productOptionId(), itemCmd.quantity(), itemCmd.unitPrice())
            )
        );
        return companyOrder;
    }

    // 단가 × 수량 → 항목 금액 계산 (buildOrder에서만 사용)
    private BigDecimal calculateItemPrice(CreateOrderCommand.OrderItemCommand item) {
        return item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
    }

    /**
     * 보상 스택 실행 (LIFO)
     * 보상 자체가 실패해도 나머지 보상은 계속 실행하고 에러를 로깅만 함
     */
    private void executeCompensations(Deque<Runnable> compensations) {
        while (!compensations.isEmpty()) {
            try {
                compensations.pop().run();
            } catch (Exception e) {
                log.error("[Saga] 보상 트랜잭션 실패 - 수동 복구 필요: {}", e.getMessage(), e);
            }
        }
    }

    // 요청한 모든 companyId에 대해 hubId 매핑이 존재하는지 검증
    private void validateHubMapping(Map<UUID, UUID> hubIdMap, List<UUID> companyIds) {
        companyIds.forEach(companyId -> {
            if (!hubIdMap.containsKey(companyId) || hubIdMap.get(companyId) == null) {
                throw new BusinessException(OrderErrorCode.HUB_MAPPING_NOT_FOUND);
            }
        });
    }

    private void validateOrderCancellable(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.ORDER_ALREADY_CANCELLED);
        }
        if (!order.isCancellable()) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    private void validateCompanyOrderCancellable(CompanyOrder companyOrder) {
        if (companyOrder.getStatus() == CompanyOrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.COMPANY_ORDER_ALREADY_CANCELLED);
        }
        if (companyOrder.getStatus() == CompanyOrderStatus.SHIPPED
                || companyOrder.getStatus() == CompanyOrderStatus.DELIVERED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    // cancelCompanyOrder: order + order.companyOrders (형제 CompanyOrder 포함)
    private CompanyOrder findCompanyOrderWithOrderAndSiblingsOrThrow(UUID companyOrderId) {
        return companyOrderRepository.findCompanyOrderWithOrderAndSiblings(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
    }
}
