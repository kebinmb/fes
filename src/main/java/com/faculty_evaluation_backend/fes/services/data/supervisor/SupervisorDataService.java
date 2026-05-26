package com.faculty_evaluation_backend.fes.services.data.supervisor;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyLoadDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyProgramLoadsDTO;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Majors;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Programs;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataRetrievalFailureException;
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
    private final PrimaryClassRepository primaryClassRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    private final UserAccountsRepository userAccountsRepository;

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public List<FacultyDTO> getFacultiesByCollegeAndStatus(String college, String status) {

        College collegeEnum = College.valueOf(college.toUpperCase());
        Status statusEnum = Status.valueOf(status.toUpperCase());

        log.info("Fetching faculties | college={} | status={}", collegeEnum, statusEnum);

        return primaryFacultyRepository.findByCollegeAndStatus(collegeEnum, statusEnum).stream().map(f -> new FacultyDTO(f.getFacultyId(), f.getLastname(), f.getFirstname(), f.getPosition(), f.getLoadLimit(), f.getMiddlename(), f.getCollege().name(), f.getStatus().name())).toList();
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public Page<FacultyLoadDTO> getFacultyLoadsByProgram(

            Long userId,

            String search,

            Pageable pageable) {

        log.info("Fetching faculty loads | userId={} | search={} | page={} | size={}", userId, search, pageable.getPageNumber(), pageable.getPageSize());

        userAccountsRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Page<FacultyLoadDTO> result = primaryClassRepository.findFacultyLoadsByProgram(userId, search, pageable);

        log.info("Faculty loads fetched successfully | totalElements={}", result.getTotalElements());

        return result;
    }

    private String extractSectionCode(Majors majors) {

        if (majors == null) {
            return null;
        }

        String value = majors.getDatabaseValue();

        if (value == null || value.isBlank() || value.equalsIgnoreCase("NONE")) {
            return null;
        }

        return value.trim();
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    @Cacheable(value = "facultyClasses", key = "#facultyId")
    public List<FacultyClassDTO> findFacultyClasses(String facultyId) {

        SchoolYearAndSemester data = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new RuntimeException("No active school year and semester found."));

        log.debug("Fetching classes | facultyId={} | schoolYear={} | semester={}", facultyId, data.getSchoolYear(), data.getSemester());

        List<FacultyClassDTO> result = primaryClassRepository.findFacultyClasses(facultyId, data.getSchoolYear(), data.getSemester().getValue());

        log.info("Classes fetched | facultyId={} | count={}", facultyId, result.size());

        return result;
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public boolean hasEvaluated(String facultyId, String evaluatorId, String classCode, String subjectCode, String yearLevel, String semester, Integer schoolYear) {
        return facultyEvaluationScoreRepository.existsByFacultyIdAndEvaluatorIdAndClassCodeAndSubjectCodeAndYearLevelAndSemesterAndSchoolYear(facultyId, evaluatorId, classCode, subjectCode, yearLevel, semester, schoolYear);
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public Page<FacultyProgramLoadsDTO> getFacultyInPrograms(

            Long userId,

            String search,

            Pageable pageable) {

        log.info("Fetching faculty program loads | userId={} | search={} | page={} | size={}", userId, search, pageable.getPageNumber(), pageable.getPageSize());

        userAccountsRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Page<FacultyProgramLoadsDTO> result = primaryClassRepository.findFacultyPerProgram(userId, search, pageable);

        log.info("Faculty program loads fetched successfully | totalElements={}", result.getTotalElements());

        return result;
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
        return new FacultyDTO(f.getFacultyId(), f.getLastname(), f.getFirstname(), f.getPosition(), f.getLoadLimit(), f.getMiddlename(), f.getCollege().name(), f.getStatus().name());
    }
}
