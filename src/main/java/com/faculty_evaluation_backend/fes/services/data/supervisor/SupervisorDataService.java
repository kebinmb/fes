package com.faculty_evaluation_backend.fes.services.data.supervisor;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupervisorDataService {
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final PrimaryClassRepository  primaryClassRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public List<FacultyDTO> getFacultiesByCollegeAndStatus(String college, String status) {

        College collegeEnum = College.valueOf(college.toUpperCase());
        Status statusEnum = Status.valueOf(status.toUpperCase());

        log.info("Fetching faculties | college={} | status={}", collegeEnum, statusEnum);

        return primaryFacultyRepository
                .findByCollegeAndStatus(collegeEnum, statusEnum)
                .stream()
                .map(f -> new FacultyDTO(
                        f.getFacultyId(),
                        f.getLastname(),
                        f.getFirstname(),
                        f.getPosition(),
                        f.getLoadLimit(),
                        f.getMiddlename(),
                        f.getCollege().name(),
                        f.getStatus().name()
                ))
                .toList();
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    @Cacheable(
            value = "facultyClasses",
            key = "#facultyId + '-' + #schoolYear + '-' + #semester"
    )
    public List<FacultyClassDTO> findFacultyClasses(
            String facultyId
    ) {
        SchoolYearAndSemesterDTO data = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE);
        log.debug(
                "Fetching classes | facultyId={} | schoolYear={} | semester={}",
                facultyId, data.getSchoolYear(), data.getSemester()
        );

        List<FacultyClassDTO> result =
                primaryClassRepository.findFacultyClasses(
                        facultyId, data.getSchoolYear(), data.getSemester()
                );

        log.info(
                "Classes fetched | facultyId={} | count={}",
                facultyId, result.size()
        );

        return result;
    }
    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public boolean hasEvaluated(
            String facultyId, String evaluatorId, String classCode,String subjectCode,String yearLevel,
            String semester, Integer schoolYear) {

        return facultyEvaluationScoreRepository
                .existsByFacultyIdAndEvaluatorIdAndClassCodeAndSubjectCodeAndYearLevelAndSemesterAndSchoolYear(
                        facultyId,
                        evaluatorId,
                        classCode,
                        subjectCode,
                        yearLevel,
                        semester,
                        schoolYear
                );
    }
    private College parseCollege(String college) {
        try {
            return College.valueOf(college.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid college value: " + college);
        }
    }

    private Status parseStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status value: " + status);
        }
    }
    private FacultyDTO mapToDTO(PrimaryFaculty f) {
        return new FacultyDTO(
                f.getFacultyId(),
                f.getLastname(),
                f.getFirstname(),
                f.getPosition(),
                f.getLoadLimit(),
                f.getMiddlename(),
                f.getCollege().name(),
                f.getStatus().name()
        );
    }
}
