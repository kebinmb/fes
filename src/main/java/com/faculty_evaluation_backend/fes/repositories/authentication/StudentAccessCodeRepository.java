package com.faculty_evaluation_backend.fes.repositories.authentication;

import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface StudentAccessCodeRepository
        extends JpaRepository<StudentAccessCode, Long> {

    Optional<StudentAccessCode> findByAccessCode(String accessCode);

    Optional<StudentAccessCode> findByStudentIdAndAccessCode(
            String studentId,
            String accessCode
    );

    boolean existsByStudentId(String studentId);

    boolean existsByStudentIdAndIsUsedTrue(String studentId);

    boolean existsByStudentIdAndIsCompletedTrue(String studentId);

    boolean existsByAccessCode(String accessCode);

    // =========================================================
    // MARIA DB SAFE PESSIMISTIC LOCK
    // =========================================================

    @Query(value = """
        SELECT *
        FROM student_access_codes
        WHERE student_id = :studentId
          AND is_used = false
          AND expires_at > :now
        ORDER BY created_at DESC
        LIMIT 1
        FOR UPDATE
        """, nativeQuery = true)
    Optional<StudentAccessCode> findLatestValidAccessCodeForUpdate(
            @Param("studentId") String studentId,
            @Param("now") Instant now
    );

    @Query(value = """
        SELECT *
        FROM student_access_codes
        WHERE student_id = :studentId
          AND is_used = false
          AND expires_at > :now
        ORDER BY created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<StudentAccessCode> findLatestValidAccessCode(
            @Param("studentId") String studentId,
            @Param("now") Instant now
    );

    // =========================================================
    // MARIA DB SAFE PESSIMISTIC LOCK
    // =========================================================

    @Query(value = """
        SELECT *
        FROM student_access_codes
        WHERE student_id = :studentId
          AND access_code = :accessCode
        LIMIT 1
        FOR UPDATE
        """, nativeQuery = true)
    Optional<StudentAccessCode> findForUpdate(
            @Param("studentId") String studentId,
            @Param("accessCode") String accessCode
    );
}
