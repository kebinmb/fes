package com.faculty_evaluation_backend.fes.services.data.students;

import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.utilities.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;

    public PageResponse<StudentClassLoadDTO> getStudentLoads(
            String studentId,
            Pageable pageable
    ) {

        log.info("========== STUDENT LOAD DEBUG START ==========");

        log.info("Incoming studentId: {}", studentId);

        log.info(
                "Pageable => page: {}, size: {}, sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );

        SchoolYearAndSemester data =
                schoolYearAndSemesterRepository
                        .findByStatus(Status.ACTIVE)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active school year and semester found."
                                )
                        );

        log.info(
                "Active School Year: {}, Semester: {}",
                data.getSchoolYear(),
                data.getSemester().getValue()
        );

        Page<StudentClassLoadDTO> result =
                primaryStudentLoadRepository.findStudentLoadDTO(
                        studentId,
                        data.getSchoolYear(),
                        data.getSemester().getValue(),
                        pageable
                );

        log.info("Total Elements: {}", result.getTotalElements());
        log.info("Total Pages: {}", result.getTotalPages());
        log.info("Returned Content Size: {}", result.getContent().size());

        result.getContent().forEach(item -> {
            log.info("""
                    
                    ---- STUDENT LOAD ITEM ----
                    classCode: {}
                    facultyId: {}
                    subjectCode: {}
                    facultyName: {}
                    subjectDescription: {}
                    semester: {}
                    schoolYear: {}
                    yearLevel: {}
                    studentId: {}
                    ---------------------------
                    """,
                    item.getClassCode(),
                    item.getFacultyId(),
                    item.getSubjectCode(),
                    item.getFacultyName(),
                    item.getSubjectDescription(),
                    item.getSemester(),
                    item.getSchoolYear(),
                    item.getYearLevel(),
                    item.getStudentId()
            );
        });

        log.info("========== STUDENT LOAD DEBUG END ==========");

        return PageMapper.toPageResponse(result);
    }
}