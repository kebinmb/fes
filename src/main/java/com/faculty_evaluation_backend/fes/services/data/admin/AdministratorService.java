package com.faculty_evaluation_backend.fes.services.data.admin;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyEvaluationScoreResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchUserAccountsResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdministratorService {

    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final UserAccountsRepository userAccountsRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;

    @Transactional
    public SchoolYearAndSemesterDTO updateSchoolYearAndSemester(Integer schoolYear, Semester semester) {
        if (schoolYear == null) {
            throw new RuntimeException("School year is required.");
        }
        if (semester == null) {

            throw new RuntimeException("Semester is required.");
        }


        schoolYearAndSemesterRepository.findBySchoolYearAndSemesterAndStatus(schoolYear, semester, Status.ACTIVE).ifPresent(existing -> {

            throw new RuntimeException("The selected school year and semester is already active.");
        });


        schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).ifPresent(existing -> {

            existing.setStatus(Status.INACTIVE);

            schoolYearAndSemesterRepository.save(existing);
        });


        SchoolYearAndSemester newRecord = SchoolYearAndSemester.builder().schoolYear(schoolYear).semester(semester).status(Status.ACTIVE).build();

        SchoolYearAndSemester savedRecord = schoolYearAndSemesterRepository.save(newRecord);


        return SchoolYearAndSemesterDTO.builder().id(savedRecord.getId()).schoolYear(savedRecord.getSchoolYear()).semester(savedRecord.getSemester()).status(savedRecord.getStatus()).createdAt(savedRecord.getCreatedAt()).build();
    }

    public PageResponse<FetchFacultyResponse> facultyList(int page, int size, String search) {

        Page<PrimaryFaculty> facultyPage;

        Pageable pageable = PageRequest.of(page, size);

        if (search != null && !search.trim().isEmpty()) {

            facultyPage = primaryFacultyRepository.findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCaseOrFacultyIdContainingIgnoreCaseOrPositionContainingIgnoreCase(search, search, search, search, pageable);

        } else {

            facultyPage = primaryFacultyRepository.findAll(pageable);

        }

        List<FetchFacultyResponse> responseList = facultyPage.getContent().stream().map(faculty -> FetchFacultyResponse.builder().facultyId(faculty.getFacultyId()).firstname(faculty.getFirstname()).lastname(faculty.getLastname()).middlename(faculty.getMiddlename()).position(faculty.getPosition()).loadLimit(faculty.getLoadLimit() != null ? faculty.getLoadLimit().toString() : null).status(faculty.getStatus()).college(faculty.getCollege()).build()).toList();

        return PageResponse.<FetchFacultyResponse>builder().content(responseList).page(facultyPage.getNumber()).size(facultyPage.getSize()).totalElements(facultyPage.getTotalElements()).totalPages(facultyPage.getTotalPages()).build();
    }

    public PageResponse<FetchUserAccountsResponse> accountList(int page, int size) {

        Page<UserAccounts> userAccountsPage = userAccountsRepository.findAll(PageRequest.of(page, size));

        List<FetchUserAccountsResponse> responseList = userAccountsPage.getContent().stream().map(user -> FetchUserAccountsResponse.builder().userId(user.getUserId()).username(user.getUsername()).email(user.getEmail()).role(user.getRole()).status(user.getStatus()).build()).toList();

        return PageResponse.<FetchUserAccountsResponse>builder().content(responseList).page(userAccountsPage.getNumber()).size(userAccountsPage.getSize()).totalElements(userAccountsPage.getTotalElements()).totalPages(userAccountsPage.getTotalPages()).build();
    }

    public PageResponse<FetchFacultyEvaluationScoreResponse> facultyEvaluationScore(int page, int size) {

        Page<FacultyEvaluationScore> scorePage = facultyEvaluationScoreRepository.findAllWithFaculty(PageRequest.of(page, size));

        List<FetchFacultyEvaluationScoreResponse> responseList = scorePage.getContent().stream().map(score -> {
            PrimaryFaculty faculty = score.getFaculty();

            return FetchFacultyEvaluationScoreResponse.builder().facultyEvaluationScoreId(score.getFacultyEvaluationScoreId()).facultyId(score.getFacultyId()).facultyName(faculty != null ? faculty.getFirstname() + " " + faculty.getLastname() : "N/A").position(score.getFaculty().getPosition()).evaluatorId(score.getEvaluatorId()).classCode(score.getClassCode()).semester(score.getSemester()).schoolYear(String.valueOf(score.getSchoolYear())).subjectCode(score.getSubjectCode()).yearLevel(score.getYearLevel()).commentsOrFeedbacks(score.getCommentsOrFeedbacks()).overallAverageScore(score.getOverallAverageScore()).overallInterpretation(score.getOverallInterpretation()).build();
        }).toList();

        return PageResponse.<FetchFacultyEvaluationScoreResponse>builder().content(responseList).page(scorePage.getNumber()).size(scorePage.getSize()).totalElements(scorePage.getTotalElements()).totalPages(scorePage.getTotalPages()).build();
    }

    public String updateFaculty(String facultyId, String firstname, String middlename, String lastname, String position, Double loadLimit, College college, Status status) {

        PrimaryFaculty faculty = primaryFacultyRepository.findByFacultyId(facultyId).orElseThrow(() -> new RuntimeException("Faculty not found"));

        int updatedRows = primaryFacultyRepository.updateFaculty(facultyId, firstname, middlename, lastname, position, loadLimit, college, status);

        if (updatedRows > 0) {
            return "Faculty updated successfully.";
        }

        return "Failed to update faculty.";
    }
}