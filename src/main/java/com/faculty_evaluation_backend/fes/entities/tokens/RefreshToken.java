package com.faculty_evaluation_backend.fes.entities.tokens;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // facultyId OR studentId
    private String userId;

    // SHA256 hash of token
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    private LocalDateTime expiryDate;

    private boolean revoked;

    private String deviceInfo;
}
