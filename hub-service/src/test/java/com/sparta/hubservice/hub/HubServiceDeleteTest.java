package com.sparta.hubservice.hub;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.hub.application.service.HubService;
import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;
import com.sparta.hubservice.hub.domain.port.CompanyReader;
import com.sparta.hubservice.hub.domain.repository.HubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HubServiceDeleteTest {

    @Mock HubRepository hubRepository;
    @Mock CompanyReader companyReader;
    @InjectMocks HubService hubService;

    private static final UUID ADMIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private Hub dummyHub() {
        return Hub.builder()
                .name("서울 허브")
                .hubType(HubType.REGIONAL)
                .address("서울시 강남구")
                .latitude(37.5)
                .longitude(127.0)
                .contactPhone("02-1234-5678")
                .status(HubStatus.ACTIVE)
                .build();
    }

    @Test
    void 소속_company가_존재하면_HUB_IN_USE_예외() {
        UUID hubId = UUID.randomUUID();
        given(hubRepository.findById(hubId)).willReturn(Optional.of(dummyHub()));
        given(companyReader.existsCompaniesByHubId(hubId)).willReturn(true);

        assertThatThrownBy(() -> hubService.deleteHub(hubId, ADMIN_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assert be.getErrorCode() == ErrorCode.HUB_IN_USE;
                });

        verify(hubRepository, never()).save(dummyHub());
    }

    @Test
    void 소속_company가_없으면_soft_delete_성공() {
        UUID hubId = UUID.randomUUID();
        Hub hub = dummyHub();
        given(hubRepository.findById(hubId)).willReturn(Optional.of(hub));
        given(companyReader.existsCompaniesByHubId(hubId)).willReturn(false);

        hubService.deleteHub(hubId, ADMIN_UUID);

        verify(hubRepository).save(hub);
    }

    @Test
    void 허브가_존재하지_않으면_HUB_NOT_FOUND_예외() {
        UUID hubId = UUID.randomUUID();
        given(hubRepository.findById(hubId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> hubService.deleteHub(hubId, ADMIN_UUID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assert be.getErrorCode() == ErrorCode.HUB_NOT_FOUND;
                });

        verify(companyReader, never()).existsCompaniesByHubId(hubId);
    }
}
