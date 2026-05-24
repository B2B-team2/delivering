package com.sparta.operationsservice.claim.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.operationsservice.claim.application.dto.ClaimCreateCommand;
import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import com.sparta.operationsservice.claim.application.dto.ClaimStatusUpdateCommand;
import com.sparta.operationsservice.claim.domain.core.ClaimStatus;
import com.sparta.operationsservice.claim.domain.core.ClaimType;
import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import com.sparta.operationsservice.global.exception.OperationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private OrderClaimRepository orderClaimRepository;

    @InjectMocks
    private ClaimService claimService;

    @Test
    @DisplayName("클레임 등록 성공")
    void createClaimSuccessTest() {
        // given
        UUID orderItemId = UUID.randomUUID();
        ClaimCreateCommand command = ClaimCreateCommand.builder()
                .orderItemId(orderItemId)
                .claimType("RETURN")
                .reason("단순 변심")
                .refundAmount(new BigDecimal("50000"))
                .build();

        OrderClaim savedClaim = OrderClaim.builder()
                .claimId(UUID.randomUUID())
                .orderItemId(orderItemId)
                .claimType(ClaimType.RETURN)
                .status(ClaimStatus.REQUESTED)
                .reason(command.getReason())
                .refundAmount(command.getRefundAmount())
                .build();

        when(orderClaimRepository.save(any(OrderClaim.class))).thenReturn(savedClaim);

        // when
        ClaimDto result = claimService.createClaim(command);

        // then
        assertThat(result.getReason()).isEqualTo(command.getReason());
        assertThat(result.getClaimType()).isEqualTo("RETURN");
        verify(orderClaimRepository, times(1)).save(any(OrderClaim.class));
    }

    @Test
    @DisplayName("클레임 목록 조회 성공")
    void getClaimsSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        OrderClaim claim = OrderClaim.builder()
                .claimId(UUID.randomUUID())
                .orderItemId(UUID.randomUUID())
                .claimType(ClaimType.EXCHANGE)
                .status(ClaimStatus.REQUESTED)
                .build();

        when(orderClaimRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(claim)));

        // when
        Page<ClaimDto> result = claimService.getClaims(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getClaimType()).isEqualTo("EXCHANGE");
    }

    @Test
    @DisplayName("클레임 상태 업데이트 성공")
    void updateClaimStatusSuccessTest() {
        // given
        UUID claimId = UUID.randomUUID();
        OrderClaim claim = OrderClaim.builder()
                .claimId(claimId)
                .orderItemId(UUID.randomUUID())
                .claimType(ClaimType.RETURN)
                .status(ClaimStatus.REQUESTED)
                .build();

        ClaimStatusUpdateCommand command = ClaimStatusUpdateCommand.builder()
                .status("PROCESSING")
                .build();

        when(orderClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        ClaimDto result = claimService.updateClaimStatus(claimId, command);

        // then
        assertThat(result.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    @DisplayName("클레임 조회 실패: 존재하지 않는 ID")
    void getClaimFail_NotFound() {
        // given
        UUID claimId = UUID.randomUUID();
        when(orderClaimRepository.findById(claimId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> claimService.getClaim(claimId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(OperationErrorCode.CLAIM_NOT_FOUND.getMessage());
    }
}
