package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.entities.primary.PrimarySection;
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
}
