package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.student.StudentSectionDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimarySection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrimarySectionRepository extends JpaRepository<PrimarySection, Long> {

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM PrimarySection s " +
           "WHERE s.sectionId = :sectionId " +
           "AND s.programCode = :programCode " +
           "AND (s.sectionCode = :sectionCode OR (s.sectionCode IS NULL AND :sectionCode IS NULL)) " +
           "AND (s.yearLevel = :yearLevel OR (s.yearLevel IS NULL AND :yearLevel IS NULL))")
    boolean existsBySectionIdAndProgramCodeAndSectionCodeAndYearLevel(
            @Param("sectionId") Integer sectionId,
            @Param("programCode") String programCode,
            @Param("sectionCode") String sectionCode,
            @Param("yearLevel") String yearLevel
    );

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );

    @Query("""
    SELECT new com.faculty_evaluation_backend.fes.dto.student.StudentSectionDTO(

        ps.programCode,

        ps.yearLevel,

        ps.sectionCode,

        COUNT(DISTINCT psl.studentId),

        COUNT(DISTINCT fes.evaluatorId),

        (
            COUNT(DISTINCT psl.studentId)
            - COUNT(DISTINCT fes.evaluatorId)
        )
    )

    FROM PrimaryStudentLoad psl

    INNER JOIN psl.primaryClass pc

    INNER JOIN pc.section ps

    LEFT JOIN FacultyEvaluationScore fes
        ON psl.studentId = fes.evaluatorId

    WHERE
        (:programCode IS NULL OR ps.programCode = :programCode)
    AND (:yearLevel IS NULL OR ps.yearLevel = :yearLevel)
    AND (:sectionCode IS NULL OR ps.sectionCode = :sectionCode)

    GROUP BY
        ps.programCode,
        ps.yearLevel,
        ps.sectionCode

    ORDER BY
        ps.programCode ASC,
        ps.yearLevel ASC,
        ps.sectionCode ASC
""")
    Page<StudentSectionDTO> getStudentSectionEvaluationData(
            @Param("programCode") String programCode,
            @Param("yearLevel") String yearLevel,
            @Param("sectionCode") String sectionCode,
            Pageable pageable
    );


}
