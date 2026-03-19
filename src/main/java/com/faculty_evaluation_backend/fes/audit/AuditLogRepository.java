package com.faculty_evaluation_backend.fes.audit;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:userId IS NULL OR a.userId = :userId)
        AND (:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:action IS NULL OR a.action = :action)
        AND (:entityType IS NULL OR a.entityType = :entityType)
        AND (:startDate IS NULL OR a.createdAt >= :startDate)
        AND (:endDate IS NULL OR a.createdAt <= :endDate)
        AND (
            :search IS NULL OR
            LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY a.createdAt DESC
    """)
    Page<AuditLog> findAuditLogs(
            Integer userId,
            String username,
            String action,
            String entityType,
            Instant startDate,
            Instant endDate,
            String search,
            Pageable pageable
    );
}
