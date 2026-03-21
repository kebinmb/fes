package com.faculty_evaluation_backend.fes.repositories.authentication;


import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.Instant;
import java.util.Optional;

@Repository
public interface StudentAccessCodeRepository extends JpaRepository<StudentAccessCode, Long> {

    Optional<StudentAccessCode> findByAccessCode(String accessCode);

    Optional<StudentAccessCode> findByStudentIdAndAccessCode(String studentId, String accessCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ac FROM StudentAccessCode ac WHERE ac.studentId = :studentId " +
            "AND ac.isUsed = false AND ac.expiresAt > :now ORDER BY ac.createdAt DESC")
    Optional<StudentAccessCode> findLatestValidAccessCodeForUpdate(String studentId, Instant now);

    boolean existsByStudentId(String studentId);

    boolean existsByStudentIdAndIsUsedTrue(String studentId);

    boolean existsByStudentIdAndIsCompletedTrue(String studentId);
    boolean existsByAccessCode(String accessCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT s FROM StudentAccessCode s
    WHERE s.studentId = :studentId
      AND s.accessCode = :accessCode
""")
    Optional<StudentAccessCode> findForUpdate(
            @Param("studentId") String studentId,
            @Param("accessCode") String accessCode
    );
}
