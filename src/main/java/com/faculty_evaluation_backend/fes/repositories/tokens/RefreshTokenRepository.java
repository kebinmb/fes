package com.faculty_evaluation_backend.fes.repositories.tokens;

import com.faculty_evaluation_backend.fes.entities.tokens.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    Optional<RefreshToken> findByTokenHashAndRevokedFalse(
            String tokenHash
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
                UPDATE RefreshToken t
                SET t.revoked = true
                WHERE t.userId = :userId
                  AND t.revoked = false
            """)
    void revokeAllByUserId(
            @Param("userId") String userId
    );

    @Query(value = """
            SELECT *
            FROM refresh_tokens
            WHERE token_hash = :hash
              AND revoked = false
            LIMIT 1
            FOR UPDATE
            """, nativeQuery = true)
    Optional<RefreshToken> findValidTokenForUpdate(
            @Param("hash") String hash
    );

    void deleteByUserId(String userId);
}