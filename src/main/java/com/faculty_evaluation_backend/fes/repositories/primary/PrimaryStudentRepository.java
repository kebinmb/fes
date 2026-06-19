package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PrimaryStudentRepository extends JpaRepository<PrimaryStudent, Long> {

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM PrimaryStudent s " +
            "WHERE s.studentId = :studentId " +
            "AND s.studentLastname = :studentLastname " +
            "AND s.studentFirstname = :studentFirstname " +
            "AND (s.studentMiddlename = :studentMiddlename OR (s.studentMiddlename IS NULL AND :studentMiddlename IS NULL))")
    boolean existsByStudentIdAndStudentLastnameAndStudentFirstnameAndStudentMiddlename(
            @Param("studentId") String studentId,
            @Param("studentLastname") String studentLastname,
            @Param("studentFirstname") String studentFirstname,
            @Param("studentMiddlename") String studentMiddlename
    );

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM PrimaryStudent s " +
            "WHERE (s.curriculumMajorId = :curriculumMajorId OR (s.curriculumMajorId IS NULL AND :curriculumMajorId IS NULL)) " +
            "AND (s.studentMiddlename = :studentMiddlename OR (s.studentMiddlename IS NULL AND :studentMiddlename IS NULL)) " +
            "AND (s.gender = :gender OR (s.gender IS NULL AND :gender IS NULL))")
    boolean existsByCurriculumMajorIdAndStudentMiddlenameAndGender(
            @Param("curriculumMajorId") Integer curriculumMajorId,
            @Param("studentMiddlename") String studentMiddlename,
            @Param("gender") String gender
    );

    Optional<PrimaryStudent> findByStudentId(String studentId);

    boolean existsByLegacyId(String legacyId);

    boolean existsByStudentId(String studentId);

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );

    @Query("""
    SELECT DISTINCT ps.studentId
    FROM PrimaryStudent ps
    JOIN PrimaryStudentLoad psl
        ON psl.studentId = ps.studentId
    WHERE ps.legacyId = :studentId
""")
    Optional<String> findByLegacyStudentIdWithLoad(
            @Param("studentId") String studentId
    );

    @Query("""
                SELECT ps.legacyId
                FROM PrimaryStudent ps
                WHERE ps.legacyId IN :ids
            """)
    Set<String> findExistingLegacyIds(
            @Param("ids") Set<String> ids
    );
}
