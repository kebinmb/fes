package com.faculty_evaluation_backend.fes.repositories.legacy;


import com.faculty_evaluation_backend.fes.entities.legacy.LegacyClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegacyClassRepository extends JpaRepository<LegacyClass, Long> {

    // Using Spring Data JPA method naming convention for embedded ID fields
    List<LegacyClass> findById_SemesterAndId_SchoolYear(String semester, Integer schoolYear);

    // Alternative: Using @Query for more explicit control
    @Query("SELECT lc FROM LegacyClass lc WHERE lc.id.semester = :semester AND lc.id.schoolYear = :schoolYear")
    List<LegacyClass> findBySemesterAndSchoolYear(
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );
}
