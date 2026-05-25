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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

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

        // 재고 예약: 실패 시 예외 전파 → 주문 생성 전체 롤백
        hubStockPort.reserveStock(order);

        // 배송 일괄 생성: 공급업체 소속 허브(출발) → 수령업체 소속 허브(도착)
        // TODO) 배송 생성 실패 시 이미 완료된 reserveStock이 자동 보상되지 않음 -> hub & delivery Feign 연동 완성 후 Saga 도입 필요
        deliveryPort.createDeliveries(order, hubIdMap, destinationHubId);

        // 선결제: 주문 생성 이벤트 발행 → PaymentEventHandler에서 결제 COMPLETED 처리 (같은 트랜잭션)
        eventPublisher.publishEvent(new OrderCreatedEvent(order.getOrderId(), order.getTotalPrice()));

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

        // 이미 CANCELLED/DELIVERED인 CompanyOrder는 건너뜀
        order.getCompanyOrders().stream()
                .filter(co -> co.getStatus() != CompanyOrderStatus.CANCELLED
                        && co.getStatus() != CompanyOrderStatus.DELIVERED)
                .forEach(co -> co.cancel(requesterId));
        order.cancel(requesterId);

        // 재고 예약 전체 취소 (orderId 기준)
        // hub-service 장애 시 예외가 전파되어 TX 전체 롤백됨 (order·payment 취소 모두 되돌아감)
        // TODO) hub Feign 연동 완성 후 Saga 도입 시 보상 처리로 전환 필요
        hubStockPort.cancelStock(orderId);

        // 주문 취소 이벤트 발행 → PaymentEventHandler에서 결제 취소 처리 (같은 트랜잭션)
        // Order·Payment가 같은 DB이므로 단일 트랜잭션으로 원자적 처리
        eventPublisher.publishEvent(new OrderCancelledEvent(orderId, requesterId));
    }

    // 서브 주문 상세 조회
    public CompanyOrderResult getCompanyOrder(UUID companyOrderId) {
        return CompanyOrderResult.from(findCompanyOrderOrThrow(companyOrderId));
    }

    // 서브 주문 부분 취소
    @Transactional
    public void cancelCompanyOrder(UUID companyOrderId, UUID requesterId) {
        CompanyOrder companyOrder = findCompanyOrderOrThrow(companyOrderId);

        validateCompanyOrderCancellable(companyOrder);

        companyOrder.cancel(requesterId);

        // 재고 예약 부분 취소 (companyOrderId 기준)
        hubStockPort.cancelCompanyStock(companyOrderId);
    }

    // 출고 준비 확인: ORDERED → PREPARING
    @Transactional
    public CompanyOrderResult prepareCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = findCompanyOrderOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.ORDERED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.prepare();
        return CompanyOrderResult.from(companyOrder);
    }

    // 출고 완료: PREPARING → SHIPPED, Order → DELIVERING
    @Transactional
    public CompanyOrderResult shipCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = findCompanyOrderOrThrow(companyOrderId);
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

        // 실재고 차감
        hubStockPort.deductStock(companyOrder);

        return CompanyOrderResult.from(companyOrder);
    }

    // 업체 주문 수령 완료: SHIPPED → DELIVERED (배송 서비스 내부 호출용)
    @Transactional
    public CompanyOrderDeliveredResult confirmDelivery(UUID companyOrderId) {
        CompanyOrder companyOrder = findCompanyOrderOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.SHIPPED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.deliver();
        companyOrder.getOrder().completeIfAllDelivered();
        return CompanyOrderDeliveredResult.from(companyOrder);
    }

    // 결제 취소 가능 여부 조회 (PaymentService → OrderQueryAdapter → OrderService)
    public boolean isCancellable(UUID orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
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
        if (order.getStatus() == OrderStatus.COMPLETED) {
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

    private CompanyOrder findCompanyOrderOrThrow(UUID companyOrderId) {
        return companyOrderRepository.findCompanyOrderById(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
    }
}
