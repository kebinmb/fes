package com.faculty_evaluation_backend.fes.entities.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_login_otp", indexes = {
        @Index(name = "idx_otp_user_email", columnList = "user_email"),
        @Index(name = "idx_otp_code", columnList = "otp_code"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class)
public class UserLoginOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    @EqualsAndHashCode.Include
    private Long otpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email", referencedColumnName = "email", nullable = false)
    @JsonIgnore
    private UserAccounts userAccount;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "used_at")
    private Instant usedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public boolean isValid() {
        return !used && Instant.now().isBefore(expiresAt);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void markAsUsed() {
        this.used = true;
        this.usedAt = Instant.now();
    }


    public void incrementAttemptCount() {
        this.attemptCount++;
    }
}
