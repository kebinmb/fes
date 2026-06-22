package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.entities.primary.PrimaryProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrimaryProgramRepository extends JpaRepository<PrimaryProgram, Long> {

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PrimaryProgram p " +
           "WHERE p.programCode = :programCode " +
           "AND p.programTitle = :programTitle " +
           "AND (p.yearGranted = :yearGranted OR (p.yearGranted IS NULL AND :yearGranted IS NULL)) " +
           "AND (p.collegeCode = :collegeCode OR (p.collegeCode IS NULL AND :collegeCode IS NULL))")
    boolean existsByProgramCodeAndProgramTitleAndYearGrantedAndCollegeCode(
            @Param("programCode") String programCode,
            @Param("programTitle") String programTitle,
            @Param("yearGranted") Integer yearGranted,
            @Param("collegeCode") String collegeCode
    );

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );

    @Query("""
                SELECT p.legacyId
                FROM PrimaryProgram p
                WHERE p.legacyDatabase = :database
            """)
    List<String> findLegacyIdsByDatabase(@Param("database") String database);
}
