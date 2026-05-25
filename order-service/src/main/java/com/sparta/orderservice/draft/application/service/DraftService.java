package com.sparta.orderservice.draft.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.application.dto.AddDraftCommand;
import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import com.sparta.orderservice.draft.application.dto.DraftResult;
import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.application.dto.DeliveryAddressInfo;
import com.sparta.orderservice.order.application.port.CompanyPort;
import com.sparta.orderservice.order.application.port.ProductPort;
import com.sparta.orderservice.order.application.service.OrderService;
import com.sparta.orderservice.order.infrastructure.client.dto.ProductOptionInfoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DraftService {

    private final DraftRepository draftRepository;
    private final OrderService orderService;
    private final ProductPort productPort;
    private final CompanyPort companyPort;

    /**
     * 임시주문 항목 추가 (upsert)
     * - 동일 userId + productOptionId가 존재하면 복원 후 수량 갱신
     * - 없으면 새로 INSERT
     */
    @Transactional
    public DraftResult addDraft(AddDraftCommand command) {
        Optional<Draft> existing = draftRepository.findExistingDraft(command.userId(), command.productOptionId());

        Draft draft;
        if (existing.isPresent()) {
            draft = existing.get();
            draft.restore(command.quantity());
        } else {
            draft = draftRepository.save(Draft.of(command.userId(), command.productId(), command.productOptionId(), command.quantity()));
        }

        return DraftResult.from(draft);
    }

    // 임시주문 목록 조회 (페이징)
    // TODO: 권한별 필터링 (마스터 → 전체, 업체담당자 → 본인 것만)
    public Page<DraftResult> getDrafts(UUID userId, Pageable pageable) {
        return draftRepository.findDraftsByUserId(userId, pageable)
                .map(DraftResult::from);
    }

    // 임시주문 항목 수량 수정
    @Transactional
    public DraftResult updateDraft(UUID draftId, int quantity, UUID userId) {
        Draft draft = findDraftOrThrow(draftId);
        checkOwnership(draft, userId);
        draft.updateQuantity(quantity);
        return DraftResult.from(draft);
    }

    // 임시주문 항목 삭제 (soft delete)
    @Transactional
    public void deleteDraft(UUID draftId, UUID userId) {
        Draft draft = findDraftOrThrow(draftId);
        checkOwnership(draft, userId);
        draft.delete(userId);
    }

    // 임시주문으로 주문 생성
    @Transactional
    public OrderResult createOrderFromDraft(CreateOrderFromDraftCommand command) {
        // 1. 임시주문 항목 조회 및 소유권 검증
        List<Draft> drafts = command.draftIds().stream()
                .map(this::findDraftOrThrow)
                .toList();
        drafts.forEach(draft -> checkOwnership(draft, command.userId()));

        // 2. Product Service → productOptionId별 (companyId, unitPrice) 조회
        List<UUID> productOptionIds = drafts.stream()
                .map(Draft::getProductOptionId)
                .toList();
        // getProductOptionInfos()가 Map<UUID, ProductOptionInfoItem>을 직접 반환
        Map<UUID, ProductOptionInfoItem> productInfoMap = productPort.getProductOptionInfos(productOptionIds);

        // 3. companyId 기준 그룹핑 → CompanyOrderCommand 목록 생성
        Map<UUID, List<Draft>> draftsByCompany = drafts.stream()
                .collect(Collectors.groupingBy(
                        draft -> productInfoMap.get(draft.getProductOptionId()).companyId()
                ));

        List<CreateOrderCommand.CompanyOrderCommand> companyOrderCommands = draftsByCompany.entrySet().stream()
                .map(entry -> new CreateOrderCommand.CompanyOrderCommand(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(draft -> new CreateOrderCommand.OrderItemCommand(
                                        draft.getProductOptionId(),
                                        draft.getQuantity(),
                                        productInfoMap.get(draft.getProductOptionId()).unitPrice()
                                ))
                                .toList()
                ))
                .toList();

        // 4. 배송지 세팅: null이면 Company Service 기본 배송지(is_default=true) 자동 조회
        // TODO: receiverCompanyId는 X-Company-Id 헤더로 주입 예정 (인증 확정 후)
        String address = command.address();
        String recipientName = command.recipientName();
        String phone = command.phone();

        if (address == null || recipientName == null || phone == null) {
            DeliveryAddressInfo defaultAddr =
                    companyPort.getDefaultDeliveryAddress(command.receiverCompanyId());
            if (address == null) {
                // Company Service 응답을 order 저장 형식 JSON으로 변환
                address = String.format(
                        "{\"address\": \"%s\", \"address_detail\": \"%s\"}",
                        defaultAddr.address(), defaultAddr.addressDetail()
                );
            }
            if (recipientName == null) recipientName = defaultAddr.recipientName();
            if (phone == null) phone = defaultAddr.phone();
        }

        // 5. 주문 생성
        CreateOrderCommand orderCommand = new CreateOrderCommand(
                command.receiverCompanyId(),
                recipientName,
                phone,
                command.slackId(),
                address,
                command.dueDate(),
                command.requestMemo(),
                companyOrderCommands
        );
        OrderResult orderResult = orderService.createOrder(orderCommand, command.userId());

        // 6. 임시주문 항목 soft delete
        drafts.forEach(draft -> draft.delete(command.userId()));

        return orderResult;
    }

    private Draft findDraftOrThrow(UUID draftId) {
        return draftRepository.findDraftById(draftId)
                .orElseThrow(() -> new BusinessException(DraftErrorCode.DRAFT_NOT_FOUND));
    }

    // 본인 임시주문 항목인지 검증
    private void checkOwnership(Draft draft, UUID userId) {
        if (!draft.getUserId().equals(userId)) {
            throw new BusinessException(DraftErrorCode.DRAFT_ACCESS_DENIED);
        }
    }
}
