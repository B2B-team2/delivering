package com.sparta.orderservice.order.domain.event;

import java.util.UUID;

/**
 * 주문 취소 도메인 이벤트
 * OrderService → PaymentEventHandler 로 결제 취소 위임
 * Order와 Payment는 같은 서비스 & DB를 공유하므로 @Transactional 원자성으로 일관성 보장
 *
 * TODO) hub, delivery 등 외부 Feign 연동이 완성되면, 보상 트랜잭션 처리를 위해 Saga 도입 검토 필요
 * 예: 배송 생성 실패 시 재고 예약 취소 보상
 */
public record OrderCancelledEvent(UUID orderId, UUID requesterId) {
}
