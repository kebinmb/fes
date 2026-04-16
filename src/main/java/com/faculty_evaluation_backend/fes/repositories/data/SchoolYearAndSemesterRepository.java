package com.faculty_evaluation_backend.fes.repositories.data;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolYearAndSemesterRepository extends JpaRepository<SchoolYearAndSemester,Long > {
    @Query("""
    SELECT new com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO(
        s.schoolYear,
        s.semester,
        s.status
    )
    FROM SchoolYearAndSemester s
    WHERE s.status = :status
""")
    SchoolYearAndSemesterDTO findByStatus(@Param("status") Status status);
}
