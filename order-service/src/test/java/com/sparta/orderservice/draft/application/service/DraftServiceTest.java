package com.sparta.orderservice.draft.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import com.sparta.orderservice.draft.application.dto.DraftResult;
import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import com.sparta.orderservice.global.dto.DeliveryAddressInfo;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.global.dto.ProductOptionInfo;
import com.sparta.orderservice.global.port.CompanyPort;
import com.sparta.orderservice.global.port.ProductPort;
import com.sparta.orderservice.order.application.service.OrderCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DraftService 단위 테스트")
class DraftServiceTest {

    @Mock
    private DraftRepository draftRepository;
    @Mock
    private DraftWriter draftWriter;
    @Mock
    private OrderCommandService orderCommandService;
    @Mock
    private ProductPort productPort;
    @Mock
    private CompanyPort companyPort;

    @InjectMocks
    private DraftService draftService;

    private UUID userId;
    private UUID receiverCompanyId;
    private UUID productOptionId;
    private UUID companyId;
    private List<UUID> draftIds;
    private CreateOrderFromDraftCommand command;
    private Draft draft;
    private OrderResult mockOrderResult;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        receiverCompanyId = UUID.randomUUID();
        productOptionId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        draftIds = List.of(UUID.randomUUID());

        draft = Draft.of(userId, UUID.randomUUID(), productOptionId, 2);

        command = new CreateOrderFromDraftCommand(
                userId, receiverCompanyId, draftIds,
                "{\"address\":\"서울시\"}", "홍길동", "010-1234-5678",
                null, LocalDateTime.now().plusDays(7), null
        );

        mockOrderResult = new OrderResult(
                UUID.randomUUID(), receiverCompanyId, "홍길동", "010-1234-5678",
                "{\"address\":\"서울시\"}", LocalDateTime.now().plusDays(7), null,
                BigDecimal.valueOf(20000), BigDecimal.ZERO, BigDecimal.valueOf(20000),
                "PENDING", List.of(), null
        );
    }

    @Nested
    @DisplayName("createOrderFromDraft() — 정상 흐름")
    class CreateOrderFromDraftSuccess {

        @Test
        @DisplayName("draft 조회 → 상품 조회 → 주문 생성 → draft 삭제 순으로 실행")
        void success_executes_all_steps_in_order() {
            when(draftWriter.readAndValidateDrafts(draftIds, userId)).thenReturn(List.of(draft));
            when(productPort.getProductOptionInfos(anyList()))
                    .thenReturn(Map.of(productOptionId, new ProductOptionInfo(productOptionId, companyId, BigDecimal.valueOf(10000))));
            when(orderCommandService.createOrder(any(), eq(userId))).thenReturn(mockOrderResult);
            doNothing().when(draftWriter).deleteAll(draftIds, userId);

            OrderResult result = draftService.createOrderFromDraft(command);

            verify(draftWriter).readAndValidateDrafts(draftIds, userId);
            verify(productPort).getProductOptionInfos(anyList());
            verify(orderCommandService).createOrder(any(), eq(userId));
            verify(draftWriter).deleteAll(draftIds, userId);
            assertThat(result.orderId()).isEqualTo(mockOrderResult.orderId());
        }

        @Test
        @DisplayName("address null이면 Company Service 기본 배송지 자동 조회")
        void null_address_fetches_default_delivery_address() {
            CreateOrderFromDraftCommand nullAddressCommand = new CreateOrderFromDraftCommand(
                    userId, receiverCompanyId, draftIds,
                    null, null, null,
                    null, LocalDateTime.now().plusDays(7), null
            );
            when(draftWriter.readAndValidateDrafts(draftIds, userId)).thenReturn(List.of(draft));
            when(productPort.getProductOptionInfos(anyList()))
                    .thenReturn(Map.of(productOptionId, new ProductOptionInfo(productOptionId, companyId, BigDecimal.valueOf(10000))));
            when(companyPort.getDefaultDeliveryAddress(receiverCompanyId))
                    .thenReturn(new DeliveryAddressInfo("{\"address\":\"기본주소\"}", "김철수", "010-9999-9999"));
            when(orderCommandService.createOrder(any(), eq(userId))).thenReturn(mockOrderResult);
            doNothing().when(draftWriter).deleteAll(draftIds, userId);

            draftService.createOrderFromDraft(nullAddressCommand);

            verify(companyPort).getDefaultDeliveryAddress(receiverCompanyId);
        }
    }

    @Nested
    @DisplayName("createOrderFromDraft() — Draft 검증 실패")
    class CreateOrderFromDraftValidation {

        @Test
        @DisplayName("draft 없음 → 예외 발생, 주문 생성 미호출")
        void draft_not_found_throws_without_creating_order() {
            when(draftWriter.readAndValidateDrafts(draftIds, userId))
                    .thenThrow(new BusinessException(DraftErrorCode.DRAFT_NOT_FOUND));

            assertThatThrownBy(() -> draftService.createOrderFromDraft(command))
                    .isInstanceOf(BusinessException.class);

            verify(orderCommandService, never()).createOrder(any(), any());
            verify(draftWriter, never()).deleteAll(any(), any());
        }

        @Test
        @DisplayName("타인 draft 접근 → 예외 발생, 주문 생성 미호출")
        void access_denied_throws_without_creating_order() {
            when(draftWriter.readAndValidateDrafts(draftIds, userId))
                    .thenThrow(new BusinessException(DraftErrorCode.DRAFT_ACCESS_DENIED));

            assertThatThrownBy(() -> draftService.createOrderFromDraft(command))
                    .isInstanceOf(BusinessException.class);

            verify(orderCommandService, never()).createOrder(any(), any());
        }
    }

    @Nested
    @DisplayName("createOrderFromDraft() — Saga 보상 흐름")
    class CreateOrderFromDraftSaga {

        @Test
        @DisplayName("주문 생성 실패 → draft 삭제 미호출, 예외 전파")
        void order_creation_fails_does_not_delete_drafts() {
            when(draftWriter.readAndValidateDrafts(draftIds, userId)).thenReturn(List.of(draft));
            when(productPort.getProductOptionInfos(anyList()))
                    .thenReturn(Map.of(productOptionId, new ProductOptionInfo(productOptionId, companyId, BigDecimal.valueOf(10000))));
            doThrow(new RuntimeException("order creation failed"))
                    .when(orderCommandService).createOrder(any(), any());

            assertThatThrownBy(() -> draftService.createOrderFromDraft(command))
                    .isInstanceOf(RuntimeException.class);

            // 주문 생성 실패 → draft는 삭제되지 않아야 함
            verify(draftWriter, never()).deleteAll(any(), any());
        }

        @Test
        @DisplayName("draft 삭제 실패 → cancelOrder 보상 실행 후 예외 전파")
        void delete_drafts_fails_triggers_cancel_order_compensation() {
            when(draftWriter.readAndValidateDrafts(draftIds, userId)).thenReturn(List.of(draft));
            when(productPort.getProductOptionInfos(anyList()))
                    .thenReturn(Map.of(productOptionId, new ProductOptionInfo(productOptionId, companyId, BigDecimal.valueOf(10000))));
            when(orderCommandService.createOrder(any(), eq(userId))).thenReturn(mockOrderResult);
            doThrow(new RuntimeException("delete failed")).when(draftWriter).deleteAll(draftIds, userId);
            doNothing().when(orderCommandService).cancelOrder(mockOrderResult.orderId(), userId);

            assertThatThrownBy(() -> draftService.createOrderFromDraft(command))
                    .isInstanceOf(RuntimeException.class);

            verify(orderCommandService).cancelOrder(mockOrderResult.orderId(), userId);
        }

        @Test
        @DisplayName("draft 삭제 실패 + cancelOrder 보상도 실패 → 로그 후 원래 예외 전파")
        void delete_and_compensation_both_fail_propagates_original_exception() {
            when(draftWriter.readAndValidateDrafts(draftIds, userId)).thenReturn(List.of(draft));
            when(productPort.getProductOptionInfos(anyList()))
                    .thenReturn(Map.of(productOptionId, new ProductOptionInfo(productOptionId, companyId, BigDecimal.valueOf(10000))));
            when(orderCommandService.createOrder(any(), eq(userId))).thenReturn(mockOrderResult);

            RuntimeException deleteException = new RuntimeException("delete failed");
            doThrow(deleteException).when(draftWriter).deleteAll(draftIds, userId);
            doThrow(new RuntimeException("cancel order also failed"))
                    .when(orderCommandService).cancelOrder(any(), any());

            assertThatThrownBy(() -> draftService.createOrderFromDraft(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("delete failed"); // 원래 예외가 전파되어야 함

            // cancelOrder 보상이 시도됐는지 확인
            verify(orderCommandService).cancelOrder(mockOrderResult.orderId(), userId);
        }
    }

    @Nested
    @DisplayName("updateDraft()")
    class UpdateDraft {

        @Test
        @DisplayName("소유자 본인이면 수량 수정 가능")
        void owner_can_update_quantity() {
            UUID draftId = UUID.randomUUID();
            Draft ownedDraft = Draft.of(userId, UUID.randomUUID(), productOptionId, 1);
            when(draftRepository.findDraftById(draftId)).thenReturn(java.util.Optional.of(ownedDraft));

            DraftResult result = draftService.updateDraft(draftId, 5, userId);

            assertThat(result.quantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("타인 draft 수정 시도 → 예외 발생")
        void other_user_cannot_update() {
            UUID draftId = UUID.randomUUID();
            Draft othersDraft = Draft.of(UUID.randomUUID(), UUID.randomUUID(), productOptionId, 1);
            when(draftRepository.findDraftById(draftId)).thenReturn(java.util.Optional.of(othersDraft));

            assertThatThrownBy(() -> draftService.updateDraft(draftId, 5, userId))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
