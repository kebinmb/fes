package com.faculty_evaluation_backend.fes.repositories.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationReport;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyEvaluationReportRepository
        extends JpaRepository<FacultyEvaluationReport, String> {

    long countByFacultyIdAndSchoolYearAndSemester(
            String facultyId,
            Integer schoolYear,
            String semester
    );

    List<FacultyEvaluationReport>
    findByFacultyIdAndSchoolYearAndSemesterAndStatus(
            String facultyId,
            Integer schoolYear,
            String semester,
            FacultyEvaluationReportStatus status
    );
}
