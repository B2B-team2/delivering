package com.sparta.orderservice.draft.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.application.dto.AddDraftCommand;
import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import com.sparta.orderservice.draft.application.dto.DraftResult;
import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import com.sparta.orderservice.draft.application.port.OrderCreatePort;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.application.dto.DeliveryAddressInfo;
import com.sparta.orderservice.order.application.dto.ProductOptionInfo;
import com.sparta.orderservice.order.application.port.CompanyPort;
import com.sparta.orderservice.order.application.port.ProductPort;
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
    private final OrderCreatePort orderCreatePort;
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

        // 2. Product Service 조회 → companyId별 CompanyOrderCommand 목록 구성
        Map<UUID, ProductOptionInfo> productInfoMap = productPort.getProductOptionInfos(
                drafts.stream().map(Draft::getProductOptionId).toList()
        );
        List<CreateOrderCommand.CompanyOrderCommand> companyOrderCommands =
                buildCompanyOrderCommands(drafts, productInfoMap);

        // 3. 배송지 세팅: null이면 Company Service 기본 배송지(is_default=true) 자동 조회
        // TODO: receiverCompanyId는 X-Company-Id 헤더로 주입 예정 (인증 확정 후)
        DeliveryAddressInfo address = resolveDeliveryAddress(command);

        // 4. 주문 생성
        OrderResult orderResult = orderCreatePort.createOrder(new CreateOrderCommand(
                command.receiverCompanyId(),
                address.recipientName(),
                address.phone(),
                command.slackId(),
                address.address(),
                command.dueDate(),
                command.requestMemo(),
                companyOrderCommands
        ), command.userId());

        // 5. 임시주문 항목 soft delete
        drafts.forEach(draft -> draft.delete(command.userId()));

        return orderResult;
    }

    // Draft 목록 + 상품 정보 → companyId 기준 그룹핑 후 CompanyOrderCommand 목록 생성
    // Draft당 productInfoMap.get() 1회 호출: companyId·unitPrice를 한 번에 추출 후 그룹핑
    private List<CreateOrderCommand.CompanyOrderCommand> buildCompanyOrderCommands(
            List<Draft> drafts, Map<UUID, ProductOptionInfo> productInfoMap) {
        return drafts.stream()
                .map(draft -> {
                    ProductOptionInfo info = productInfoMap.get(draft.getProductOptionId());
                    return Map.entry(
                            info.companyId(),
                            new CreateOrderCommand.OrderItemCommand(
                                    draft.getProductOptionId(),
                                    draft.getQuantity(),
                                    info.unitPrice()
                            )
                    );
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> new CreateOrderCommand.CompanyOrderCommand(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 배송지 null-fallback: 요청값이 하나라도 null이면 Company Service 기본 배송지 자동 조회
    // address는 CompanyAdapter에서 jsonb 형식으로 변환 완료된 값 그대로 사용
    private DeliveryAddressInfo resolveDeliveryAddress(CreateOrderFromDraftCommand command) {
        String address = command.address();
        String recipientName = command.recipientName();
        String phone = command.phone();

        if (address == null || recipientName == null || phone == null) {
            DeliveryAddressInfo defaultAddr = companyPort.getDefaultDeliveryAddress(command.receiverCompanyId());
            if (address == null) address = defaultAddr.address();
            if (recipientName == null) recipientName = defaultAddr.recipientName();
            if (phone == null) phone = defaultAddr.phone();
        }
        return new DeliveryAddressInfo(address, recipientName, phone);
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
