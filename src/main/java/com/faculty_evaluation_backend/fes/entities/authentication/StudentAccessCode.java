package com.faculty_evaluation_backend.fes.entities.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "student_access_codes", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_access_code", columnList = "access_code"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class)
public class StudentAccessCode{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_code_id")
    private Long accessCodeId;

    // Reference to StudentAccounts (if created)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_account_id", referencedColumnName = "student_account_id")
    @JsonIgnore
    private StudentAccounts studentAccount;

    // Reference to PrimaryStudent (NO JPA relationship - just store the ID)
    @Column(name = "student_id", nullable = false, length = 15)
    private String studentId;

    @Column(name = "access_code", nullable = false, unique = true, length = 20)
    private String accessCode;

    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Helper methods
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isUsed && !isExpired();
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = Instant.now();
    }

    public void markAsCompleted() {
        this.isCompleted = true;
        this.completedAt = Instant.now();
    }
}