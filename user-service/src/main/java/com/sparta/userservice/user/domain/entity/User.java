package com.sparta.userservice.user.domain.entity;

import com.sparta.common.entity.BaseEntity;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;

import com.sparta.userservice.user.domain.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "slack_id", length = 36)
    private String slackId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private ApprovalStatus approvalStatus;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    public static User create(String email, String encodedPassword, String name, String phone, String slackId, Role role) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.phone = phone;
        user.slackId = slackId;
        user.role = role;
        user.approvalStatus = ApprovalStatus.PENDING;
        return user;
    }

    public void approve(UUID adminId) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = adminId;
        this.approvedAt = LocalDateTime.now();
        this.rejectedReason = null;
    }

    public void reject(UUID adminId, String reason) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvedBy = adminId;
        this.rejectedReason = reason;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String name, String phone, String slackId) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (slackId != null) this.slackId = slackId;
    }

    // TODO: BaseEntity.deletedBy 타입 String/UUID 통일 후 제거 예정
    public void softDelete(String deletedBy) {
        super.softDelete(UUID.nameUUIDFromBytes(deletedBy.getBytes()));
    }
}