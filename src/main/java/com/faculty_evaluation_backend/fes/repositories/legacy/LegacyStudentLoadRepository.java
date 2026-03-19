package com.faculty_evaluation_backend.fes.repositories.legacy;


import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacyStudentLoadId;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudentLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegacyStudentLoadRepository extends JpaRepository<LegacyStudentLoad, LegacyStudentLoadId> {
    @Query(value = """
        SELECT sl.student_id,
               sl.yearlevel,
               sl.class_code,
               c.faculty_id,
               c.subject_code,
               c.section_id,
               c.semester,
               c.school_year,
               sl.load_id
        FROM student_load sl
        INNER JOIN class c ON sl.class_code = c.class_code
        WHERE c.school_year = :schoolYear AND c.semester = :semester
        """, nativeQuery = true)
    List<Object[]> findStudentLoadsWithClassInfoBySchoolYearAndSemester(
            @Param("schoolYear") String schoolYear,
            @Param("semester") String semester
    );

}
