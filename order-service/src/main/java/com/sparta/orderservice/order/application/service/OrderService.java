package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.CompanyOrderResult;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.CompanyOrderDeliveredResult;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderItem;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.event.OrderCancelledEvent;
import com.sparta.orderservice.order.domain.event.OrderCreatedEvent;
import com.sparta.orderservice.order.application.port.CompanyPort;
import com.sparta.orderservice.order.application.port.DeliveryPort;
import com.sparta.orderservice.order.application.port.HubStockPort;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
public class OrderService {

    private final OrderRepository orderRepository;
    private final CompanyOrderRepository companyOrderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final HubStockPort hubStockPort;
    private final CompanyPort companyPort;
    private final DeliveryPort deliveryPort;

    // 주문 생성
    @Transactional
    public OrderResult createOrder(CreateOrderCommand command, UUID requesterId) {
        Order order = buildOrder(command);
        orderRepository.save(order);

        // 수령업체 + 공급업체 ID 전체 → companyId : hubId 매핑 일괄 조회 (단 1회 호출)
        List<UUID> allCompanyIds = Stream.concat(
                Stream.of(order.getReceiverCompanyId()),
                order.getCompanyOrders().stream().map(CompanyOrder::getCompanyId)
        ).toList();
        Map<UUID, UUID> hubIdMap = companyPort.getHubIds(allCompanyIds);
        validateHubMapping(hubIdMap, allCompanyIds);
        UUID destinationHubId = hubIdMap.get(order.getReceiverCompanyId()); // 수령업체 소속 허브

        // Saga 보상 스택: 외부 호출 성공 시 역순 보상 등록, 예외 발생 시 LIFO 순으로 실행
        Deque<Runnable> compensations = new ArrayDeque<>();
        try {
            // (1) 재고 예약 - 보상을 먼저 등록
            // reserveStock: CompanyOrder 수만큼 루프 호출 -> 중간 실패 시 부분 예약된 재고가 hub-service에 남을 수 있음
            // → cancelStock으로 정리
            compensations.push(() -> hubStockPort.cancelStock(order.getOrderId()));
            hubStockPort.reserveStock(order);

            // (2)) 배송 일괄 생성 → 실패 시 (1) 보상 (재고 예약 취소)
            Map<UUID, UUID> deliveryMap = deliveryPort.createDeliveries(order, hubIdMap, destinationHubId);
            compensations.push(() -> deliveryPort.cancelDeliveries(order.getOrderId()));

            // 배송 ID 할당 (OrderItem ↔ Delivery 추적용)
            order.getCompanyOrders().forEach(co -> {
                UUID deliveryId = deliveryMap.get(co.getCompanyOrderId());
                if (deliveryId != null) {
                    co.getOrderItems().forEach(item -> item.assignDelivery(deliveryId));
                }
            });

            // 선결제: 주문 생성 이벤트 발행 → PaymentEventHandler에서 결제 COMPLETED 처리 (같은 트랜잭션)
            // 실패 시 (2) -> (1) 순으로 보상 실행 후 예외 re-throw → TX 롤백
            eventPublisher.publishEvent(new OrderCreatedEvent(order.getOrderId(), order.getTotalPrice()));

        } catch (Exception e) {
            executeCompensations(compensations);
            throw e;
        }

        return OrderResult.from(order);
    }

    /**
     * 전체 주문 조회 (페이징)
     * TODO: 권한별 필터링
     * - 마스터 → 전체, 허브관리자 → 담당 허브 소속 업체 전체, 업체 담당자 → 자기 회사 주문만)
     */
    public Page<OrderResult> getOrders(UUID requesterId, Pageable pageable) {
        return orderRepository.findAllOrders(pageable).map(OrderResult::from);
    }

    // 주문 단건 상세 조회
    public OrderResult getOrder(UUID orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResult.from(order);
    }

    // 주문 전체 취소
    @Transactional
    public void cancelOrder(UUID orderId, UUID requesterId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

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

    // 서브 주문 상세 조회
    public CompanyOrderResult getCompanyOrder(UUID companyOrderId) {
        return CompanyOrderResult.from(findCompanyOrderWithItemsAndOrderOrThrow(companyOrderId));
    }

    // 서브 주문 부분 취소
    @Transactional
    public void cancelCompanyOrder(UUID companyOrderId, UUID requesterId) {
        CompanyOrder companyOrder = findCompanyOrderWithOrderAndSiblingsOrThrow(companyOrderId);

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

    // 출고 준비 확인: ORDERED → PREPARING
    @Transactional
    public CompanyOrderResult prepareCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = findCompanyOrderWithItemsAndOrderOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.ORDERED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.prepare();
        return CompanyOrderResult.from(companyOrder);
    }

    // 출고 완료: PREPARING → SHIPPED, Order → DELIVERING
    @Transactional
    public CompanyOrderResult shipCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = findCompanyOrderWithItemsAndOrderOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.PREPARING) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.ship();

        // CompanyOrder가 출고되면 Order → DELIVERING 전환
        // (이미 DELIVERING/COMPLETED 상태면 중복 전환 방지)
        Order order = companyOrder.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.startDelivery();
        }

        // 실재고 차감 (마지막 외부 호출 → 실패 시 TX 롤백으로 DB 복원, 별도 보상 불필요)
        hubStockPort.deductStock(companyOrder);

        return CompanyOrderResult.from(companyOrder);
    }

    // 업체 주문 수령 완료: SHIPPED → DELIVERED (배송 서비스 내부 호출용)
    @Transactional
    public CompanyOrderDeliveredResult confirmDelivery(UUID companyOrderId) {
        CompanyOrder companyOrder = findCompanyOrderWithOrderAndSiblingsOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.SHIPPED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.deliver();
        companyOrder.getOrder().updateStatus(null); // 배송 완료 시에는 삭제자 정보 없음
        return CompanyOrderDeliveredResult.from(companyOrder);
    }

    // 결제 취소 가능 여부 조회 (PaymentService → OrderQueryAdapter → OrderService)
    public boolean isCancellable(UUID orderId) {
        return orderRepository.findOrderById(orderId)
                .map(this::checkOrderCancellable)
                .orElse(false);
    }

    private boolean checkOrderCancellable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) return false;
        return order.getCompanyOrders().stream()
                .noneMatch(co -> co.getStatus() == CompanyOrderStatus.SHIPPED
                        || co.getStatus() == CompanyOrderStatus.DELIVERED);
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
        if (!checkOrderCancellable(order)) {
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

    // getCompanyOrder + prepareCompanyOrder + shipCompanyOrder: orderItems + order
    private CompanyOrder findCompanyOrderWithItemsAndOrderOrThrow(UUID companyOrderId) {
        return companyOrderRepository.findCompanyOrderWithItemsAndOrder(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
    }

    // cancelCompanyOrder + confirmDelivery: order + order.companyOrders
    private CompanyOrder findCompanyOrderWithOrderAndSiblingsOrThrow(UUID companyOrderId) {
        return companyOrderRepository.findCompanyOrderWithOrderAndSiblings(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
    }
}
