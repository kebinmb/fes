package com.faculty_evaluation_backend.fes.audit;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    @Query(
            value = """
        SELECT
            a.id AS id,
            a.userId AS userId,
            a.username AS username,
            a.entityType AS entityType,
            a.entityId AS entityId,
            a.action AS action,
            a.ipAddress AS ipAddress,
            a.userAgent AS userAgent,
            a.requestMethod AS requestMethod,
            a.requestPath AS requestPath,
            a.executionTimeMs AS executionTimeMs,
            a.oldValue AS oldValue,
            a.newValue AS newValue,
            a.createdAt AS createdAt
        FROM AuditLog a
        WHERE (:userId IS NULL OR a.userId = :userId)
        AND (:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (
            :action IS NULL
            OR a.action = :action
            OR (:action = 'FAILED' AND a.action LIKE CONCAT('%', 'FAILED'))
        )
        AND (:entityType IS NULL OR a.entityType = :entityType)
        AND (:startDate IS NULL OR a.createdAt >= :startDate)
        AND (:endDate IS NULL OR a.createdAt <= :endDate)
        AND (
            :search IS NULL OR
            LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.requestMethod) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.requestPath) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """,
            countQuery = """
        SELECT COUNT(a)
        FROM AuditLog a
        WHERE (:userId IS NULL OR a.userId = :userId)
        AND (:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (
            :action IS NULL
            OR a.action = :action
            OR (:action = 'FAILED' AND a.action LIKE CONCAT('%', 'FAILED'))
        )
        AND (:entityType IS NULL OR a.entityType = :entityType)
        AND (:startDate IS NULL OR a.createdAt >= :startDate)
        AND (:endDate IS NULL OR a.createdAt <= :endDate)
        AND (
            :search IS NULL OR
            LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.requestMethod) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.requestPath) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """
    )
    Page<AuditLogSummaryProjection> findAuditLogs(
            @Param("userId") Integer userId,
            @Param("username") String username,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT
            a.id AS id,
            a.userId AS userId,
            a.username AS username,
            a.entityType AS entityType,
            a.entityId AS entityId,
            a.action AS action,
            a.ipAddress AS ipAddress,
            a.userAgent AS userAgent,
            a.requestMethod AS requestMethod,
            a.requestPath AS requestPath,
            a.executionTimeMs AS executionTimeMs,
            a.oldValue AS oldValue,
            a.newValue AS newValue,
            a.createdAt AS createdAt
        FROM AuditLog a
        WHERE (:userId IS NULL OR a.userId = :userId)
        AND (:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (
            :action IS NULL
            OR a.action = :action
            OR (:action = 'FAILED' AND a.action LIKE CONCAT('%', 'FAILED'))
        )
        AND (:entityType IS NULL OR a.entityType = :entityType)
        AND (:startDate IS NULL OR a.createdAt >= :startDate)
        AND (:endDate IS NULL OR a.createdAt <= :endDate)
        AND (
            :search IS NULL OR
            LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.requestMethod) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(a.requestPath) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Slice<AuditLogSummaryProjection> findAuditLogSlice(
            @Param("userId") Integer userId,
            @Param("username") String username,
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("search") String search,
            Pageable pageable
    );
}
