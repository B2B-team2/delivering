package com.sparta.orderservice.draft.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.application.dto.AddDraftCommand;
import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import com.sparta.orderservice.draft.application.dto.DraftResult;
import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import com.sparta.orderservice.global.security.SecurityUtils;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.global.port.CompanyPort;
import com.sparta.orderservice.global.dto.DeliveryAddressInfo;
import com.sparta.orderservice.global.dto.ProductOptionInfo;
import com.sparta.orderservice.global.port.ProductPort;
import com.sparta.orderservice.order.application.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DraftService {

    private final DraftRepository draftRepository;
    private final DraftWriter draftWriter;
    private final OrderCommandService orderCommandService;
    private final ProductPort productPort;
    private final CompanyPort companyPort;
    private final SecurityUtils securityUtils;

    /**
     * 임시주문 항목 추가 (upsert)
     * - 동일 userId + productOptionId가 존재하면 복원 후 수량 갱신
     * - 없으면 새로 INSERT
     *
     * TX 없이 DraftWriter의 독립 TX 메서드를 호출:
     * 동시 요청으로 DataIntegrityViolationException 발생 시 새 TX로 재조회 후 수량 갱신
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DraftResult addDraft(AddDraftCommand command) {
        if (!securityUtils.isMaster() && !securityUtils.isCompanyManager()) {
            throw new BusinessException(DraftErrorCode.FORBIDDEN);
        }
        try {
            return draftWriter.tryInsertOrUpdate(command);
        } catch (DataIntegrityViolationException e) {
            // 동시 INSERT 충돌 → 새 TX에서 재조회 후 수량 갱신
            log.warn("[Draft] 동시 INSERT 충돌 감지, 재조회 후 수량 갱신: userId={}, productOptionId={}",
                    command.userId(), command.productOptionId());
            return draftWriter.findAndRestore(command);
        }
    }

    public Page<DraftResult> getDrafts(UUID userId, Pageable pageable) {
        if (securityUtils.isMaster()) {
            return draftRepository.findAllDrafts(pageable).map(DraftResult::from);
        }
        if (securityUtils.isCompanyManager()) {
            return draftRepository.findDraftsByUserId(userId, pageable).map(DraftResult::from);
        }
        throw new BusinessException(DraftErrorCode.FORBIDDEN);
    }

    // 임시주문 항목 수량 수정
    @Transactional
    public DraftResult updateDraft(UUID draftId, int quantity, UUID userId) {
        if (!securityUtils.isMaster() && !securityUtils.isCompanyManager()) {
            throw new BusinessException(DraftErrorCode.FORBIDDEN);
        }
        Draft draft = findDraftOrThrow(draftId);
        checkOwnership(draft, userId);
        draft.updateQuantity(quantity);
        return DraftResult.from(draft);
    }

    // 임시주문 항목 삭제 (soft delete)
    @Transactional
    public void deleteDraft(UUID draftId, UUID userId) {
        if (!securityUtils.isMaster() && !securityUtils.isCompanyManager()) {
            throw new BusinessException(DraftErrorCode.FORBIDDEN);
        }
        Draft draft = findDraftOrThrow(draftId);
        checkOwnership(draft, userId);
        draft.delete(userId);
    }

    /**
     * 임시주문으로 주문 생성 (Saga)
     *
     * TX를 갖지 않고 각 단계를 독립 TX로 분리:
     * - 외부 호출(hub/delivery)이 DB TX를 점유하지 않도록 NOT_SUPPORTED 사용
     * - draft 삭제 실패 시 이미 생성된 주문을 보상 취소
     */
    // MASTER → 전체 / COMPANY_MANAGER → 본인 draft만 (서비스 레이어에서 검증)
    // receiverCompanyId null 검증은 OrderCommandService.createOrder에서 처리
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OrderResult createOrderFromDraft(CreateOrderFromDraftCommand command) {
        if (!securityUtils.isMaster() && !securityUtils.isCompanyManager()) {
            throw new BusinessException(DraftErrorCode.FORBIDDEN);
        }
        // 1. 임시주문 항목 조회 및 소유권 검증 (독립 readOnly TX)
        List<Draft> drafts = draftWriter.readAndValidateDrafts(command.draftIds(), command.userId());

        // 2. Product Service 조회 → companyId별 CompanyOrderCommand 목록 구성
        Map<UUID, ProductOptionInfo> productInfoMap = productPort.getProductOptionInfos(
                drafts.stream().map(Draft::getProductOptionId).toList()
        );
        List<CreateOrderCommand.CompanyOrderCommand> companyOrderCommands =
                buildCompanyOrderCommands(drafts, productInfoMap);

        // 3. 배송지 세팅: null이면 Company Service 기본 배송지(is_default=true) 자동 조회
        DeliveryAddressInfo address = resolveDeliveryAddress(command);

        // 4. 주문 생성 (독립 TX + 내부 hub/delivery Saga)
        OrderResult orderResult = orderCommandService.createOrder(new CreateOrderCommand(
                command.receiverCompanyId(),
                address.recipientName(),
                address.phone(),
                command.slackId(),
                address.address(),
                command.dueDate(),
                command.requestMemo(),
                companyOrderCommands
        ), command.userId());

        // 5. 임시주문 항목 soft delete (독립 TX)
        // 실패 시 Saga 보상: 생성된 주문 취소 (hub 재고 + 배송 취소 포함)
        try {
            draftWriter.deleteAll(command.draftIds(), command.userId());
        } catch (Exception e) {
            log.error("[Saga] draft 삭제 실패, 주문 취소 보상 실행: orderId={}", orderResult.orderId(), e);
            try {
                orderCommandService.cancelOrder(orderResult.orderId(), command.userId());
            } catch (Exception compensationEx) {
                log.error("[Saga] 주문 취소 보상 실패 - 수동 복구 필요: orderId={}", orderResult.orderId(), compensationEx);
            }
            throw e;
        }

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

    // 본인 임시주문 항목인지 검증 — MASTER는 소유권 검사 건너뜀
    private void checkOwnership(Draft draft, UUID userId) {
        if (securityUtils.isMaster()) return;
        if (!draft.getUserId().equals(userId)) {
            throw new BusinessException(DraftErrorCode.DRAFT_ACCESS_DENIED);
        }
    }
}
