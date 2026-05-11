package com.faculty_evaluation_backend.fes.repositories.data;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolYearAndSemesterRepository extends JpaRepository<SchoolYearAndSemester, Long> {
    Optional<SchoolYearAndSemester> findByStatus(
            Status status
    );

    Optional<SchoolYearAndSemester>
    findBySchoolYearAndSemesterAndStatus(
            Integer schoolYear,
            Semester semester,
            Status status
    );
}
