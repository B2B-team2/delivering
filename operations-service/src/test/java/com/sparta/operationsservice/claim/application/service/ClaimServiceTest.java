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
    @DisplayName("클레임 생성 성공: 유효한 명령 수신 시 Repository의 save가 호출되는가?")
    void createClaimSuccessTest() {
        // given
        ClaimCreateCommand command = ClaimCreateCommand.builder()
                .orderItemId(UUID.randomUUID())
                .claimType("RETURN")
                .reason("Test reason")
                .refundAmount(BigDecimal.valueOf(10000))
                .build();

        OrderClaim savedClaim = OrderClaim.builder()
                .claimId(UUID.randomUUID())
                .orderItemId(command.getOrderItemId())
                .claimType(ClaimType.RETURN)
                .reason(command.getReason())
                .refundAmount(command.getRefundAmount())
                .status(ClaimStatus.REQUESTED)
                .build();

        when(orderClaimRepository.save(any(OrderClaim.class))).thenReturn(savedClaim);

        // when
        ClaimDto result = claimService.createClaim(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReason()).isEqualTo(command.getReason());
        verify(orderClaimRepository, times(1)).save(any(OrderClaim.class));
    }

    @Test
    @DisplayName("클레임 목록 조회: 페이징 처리가 올바르게 수행되는가?")
    void getClaimsTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        OrderClaim claim = OrderClaim.builder()
                .claimId(UUID.randomUUID())
                .orderItemId(UUID.randomUUID())
                .claimType(ClaimType.RETURN)
                .status(ClaimStatus.REQUESTED)
                .reason("reason")
                .refundAmount(BigDecimal.ZERO)
                .build();
        Page<OrderClaim> page = new PageImpl<>(List.of(claim));

        when(orderClaimRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<ClaimDto> result = claimService.getClaims(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(orderClaimRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("클레임 상세 조회 성공: 존재하는 ID로 조회 시 DTO가 반환되는가?")
    void getClaimSuccessTest() {
        // given
        UUID claimId = UUID.randomUUID();
        OrderClaim claim = OrderClaim.builder()
                .claimId(claimId)
                .orderItemId(UUID.randomUUID())
                .claimType(ClaimType.RETURN)
                .status(ClaimStatus.REQUESTED)
                .reason("reason")
                .refundAmount(BigDecimal.ZERO)
                .build();

        when(orderClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        ClaimDto result = claimService.getClaim(claimId);

        // then
        assertThat(result).isNotNull();
        verify(orderClaimRepository, times(1)).findById(claimId);
    }

    @Test
    @DisplayName("클레임 상세 조회 실패: 존재하지 않는 ID 조회 시 CLAIM_NOT_FOUND 예외가 발생하는가?")
    void getClaimNotFoundTest() {
        // given
        UUID claimId = UUID.randomUUID();
        when(orderClaimRepository.findById(claimId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> claimService.getClaim(claimId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", OperationErrorCode.CLAIM_NOT_FOUND);
    }

    @Test
    @DisplayName("클레임 상태 업데이트 성공: 유효한 상태 변경 시 상태가 반영되는가?")
    void updateClaimStatusSuccessTest() {
        // given
        UUID claimId = UUID.randomUUID();
        OrderClaim claim = OrderClaim.builder()
                .claimId(claimId)
                .orderItemId(UUID.randomUUID())
                .claimType(ClaimType.RETURN)
                .status(ClaimStatus.REQUESTED)
                .reason("reason")
                .refundAmount(BigDecimal.ZERO)
                .build();
        ClaimStatusUpdateCommand command = ClaimStatusUpdateCommand.builder()
                .status("COMPLETED")
                .build();

        when(orderClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when
        ClaimDto result = claimService.updateClaimStatus(claimId, command);

        // then
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.COMPLETED);
    }

    @Test
    @DisplayName("클레임 상태 업데이트 실패: 잘못된 상태 문자열 전달 시 INVALID_CLAIM_STATUS 예외가 발생하는가?")
    void updateClaimStatusInvalidStatusTest() {
        // given
        UUID claimId = UUID.randomUUID();
        OrderClaim claim = OrderClaim.builder().claimId(claimId).build();
        ClaimStatusUpdateCommand command = ClaimStatusUpdateCommand.builder()
                .status("INVALID_STATUS")
                .build();

        when(orderClaimRepository.findById(claimId)).thenReturn(Optional.of(claim));

        // when & then
        assertThatThrownBy(() -> claimService.updateClaimStatus(claimId, command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", OperationErrorCode.INVALID_CLAIM_STATUS);
    }
}
