package com.sparta.userservice.user.domain.entity;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_company_managers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyManager extends BaseEntity {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "company_id") // nullable - 승인 시점에 업체 연결
    private UUID companyId;

    public static CompanyManager create(User user) {
        CompanyManager manager = new CompanyManager();
        manager.user = user;
        return manager;
    }

    public void assignCompany(UUID companyId) {
        this.companyId = companyId;
    }
}