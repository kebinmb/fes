package com.faculty_evaluation_backend.fes.entities.authentication;

import com.faculty_evaluation_backend.fes.entities.primary.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_accounts", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_email", columnList = "email")
})
@EntityListeners(AuditingEntityListener.class)
public class UserAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @EqualsAndHashCode.Include
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private Role role;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "password_changed")
    private Instant passwordChangedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @JsonIgnore
    @Column(name = "reset_token")
    private String resetToken;

    @JsonIgnore
    @Column(name = "reset_token_expiry")
    private Instant resetTokenExpiry;

    @JsonIgnore
    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserLoginOtp> otps = new ArrayList<>();

    @Column(name = "college")
    @Enumerated(EnumType.STRING)
    private College college;

    @Column(name = "program")
    @Enumerated(EnumType.STRING)
    private Programs programs;

    @Column(name = "major")
    @Enumerated(EnumType.STRING)
    private Majors majors;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "data_source")
    private String dataSource;

    public void addOtp(UserLoginOtp otp) {
        otps.add(otp);
        otp.setUserAccount(this);
    }

    public void removeOtp(UserLoginOtp otp) {
        otps.remove(otp);
        otp.setUserAccount(null);
    }

    public void lastLoginNow() {
        this.lastLoginAt = Instant.now();
    }
}