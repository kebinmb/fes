package com.faculty_evaluation_backend.fes.repositories.primary;

import com.faculty_evaluation_backend.fes.entities.primary.PrimarySubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrimarySubjectRepository extends JpaRepository<PrimarySubject, Long> {
    Optional<PrimarySubject> findBySubjectCode(String subjectCode);

    boolean existsBySubjectCode(String subjectCode);

    boolean existsBySubjectCodeAndPrimarySubjectIdNot(String subjectCode, Long primarySubjectId);

    @Query("""
        SELECT s FROM PrimarySubject s
        WHERE :search IS NULL OR :search = ''
           OR LOWER(COALESCE(s.subjectCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(COALESCE(s.descriptiveTitle, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY s.subjectCode ASC
    """)
    Page<PrimarySubject> searchSubjects(@Param("search") String search, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM PrimarySubject s " +
            "WHERE s.subjectCode = :subjectCode " +
            "AND s.descriptiveTitle = :descriptiveTitle")
    boolean existsBySubjectCodeAndDescriptiveTitle(
            @Param("subjectCode") String subjectCode,
            @Param("descriptiveTitle") String descriptiveTitle
    );

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );

    @Query("""
    SELECT ps.legacyId
    FROM PrimarySubject ps
    WHERE ps.legacyDatabase = :database
""")
    List<String> findLegacyIdsByDatabase(String database);

    @Modifying
    @Query(value = """
        UPDATE primary_subject ps
        INNER JOIN (
            SELECT 
                fw.course_code,
                MAX(fw.total_hours_per_week) AS best_hours
            FROM faculty_workload fw
            WHERE fw.course_code IS NOT NULL 
              AND TRIM(fw.course_code) != '' 
              AND fw.total_hours_per_week > 0
            GROUP BY fw.course_code
        ) t ON t.course_code = ps.subject_code
        SET 
            ps.lec_units = CASE 
                WHEN t.best_hours = 3.0 THEN 3.0
                WHEN t.best_hours = 2.0 THEN 2.0
                WHEN t.best_hours = 1.0 THEN 1.0
                WHEN t.best_hours = 4.0 THEN 4.0
                WHEN t.best_hours IN (4.25, 4.5) THEN 2.0
                WHEN t.best_hours = 5.0 THEN 2.0
                WHEN t.best_hours IN (3.25, 3.5) THEN 1.0
                WHEN t.best_hours IN (5.25, 5.5) THEN 3.0
                WHEN t.best_hours >= 6.0 AND t.best_hours < 7.0 THEN 6.0
                WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN 3.0
                ELSE t.best_hours
            END,
            ps.lab_units = CASE 
                WHEN t.best_hours IN (4.25, 4.5, 5.0, 3.25, 3.5, 5.25, 5.5) THEN 1.0
                WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN 2.0
                ELSE 0.0
            END,
            ps.lec_hours = CASE 
                WHEN t.best_hours = 3.0 THEN 3.0
                WHEN t.best_hours = 2.0 THEN 2.0
                WHEN t.best_hours = 1.0 THEN 1.0
                WHEN t.best_hours = 4.0 THEN 4.0
                WHEN t.best_hours IN (4.25, 4.5) THEN ROUND(t.best_hours - 3.0, 2)
                WHEN t.best_hours = 5.0 THEN 2.0
                WHEN t.best_hours IN (3.25, 3.5) THEN ROUND(t.best_hours - 2.0, 2)
                WHEN t.best_hours IN (5.25, 5.5) THEN ROUND(t.best_hours - 3.0, 2)
                WHEN t.best_hours >= 6.0 AND t.best_hours < 7.0 THEN t.best_hours
                WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN ROUND(t.best_hours - 4.5, 2)
                ELSE t.best_hours
            END,
            ps.lab_hours = CASE 
                WHEN t.best_hours IN (4.25, 4.5, 5.0, 5.25, 5.5) THEN 3.0
                WHEN t.best_hours IN (3.25, 3.5) THEN 2.0
                WHEN t.best_hours >= 7.0 AND t.best_hours <= 8.5 THEN 4.5
                ELSE 0.0
            END
        WHERE (:onlyNullOrZero = false 
               OR (ps.lec_hours IS NULL OR ps.lec_hours = 0) 
                  AND (ps.lab_hours IS NULL OR ps.lab_hours = 0))
    """, nativeQuery = true)
    int syncSubjectHoursFromWorkload(@Param("onlyNullOrZero") boolean onlyNullOrZero);
}

