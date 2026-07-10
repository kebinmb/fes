package com.faculty_evaluation_backend.fes.entities.tokens;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token_hash", columnList = "tokenHash"),
        @Index(name = "idx_user_id", columnList = "userId")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    private Instant expiryDate;

    @Column(nullable = false)
    private Instant lastActivityAt;

    private boolean revoked;

    private String deviceInfo;
}
