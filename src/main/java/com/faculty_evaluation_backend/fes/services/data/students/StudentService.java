package com.faculty_evaluation_backend.fes.services.data.students;

import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.utilities.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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

        System.out.println("🔥🔥🔥 STUDENT LOAD METHOD HIT 🔥🔥🔥");

        log.error("🔥🔥🔥 STUDENT LOAD METHOD HIT 🔥🔥🔥");

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

        // =========================================================
        // RAW ENTITY CHECK
        // =========================================================

        List<PrimaryStudentLoad> rawLoads =
                primaryStudentLoadRepository.findAll()
                        .stream()
                        .filter(x -> studentId.equals(x.getStudentId()))
                        .toList();

        log.error("RAW ENTITY COUNT: {}", rawLoads.size());

        rawLoads.forEach(load -> {
            log.error("""
                    
                    RAW LOAD
                    primaryStudentLoadId: {}
                    classCode: {}
                    studentId: {}
                    yearLevel: {}
                    """,
                    load.getPrimaryStudentLoadId(),
                    load.getClassCode(),
                    load.getStudentId(),
                    load.getYearLevel()
            );
        });

        // =========================================================
        // PAGE QUERY
        // =========================================================

        Page<StudentClassLoadDTO> result =
                primaryStudentLoadRepository.findStudentLoadDTO(
                        studentId,
                        data.getSchoolYear(),
                        data.getSemester().getValue(),
                        pageable
                );

        log.error("PAGE QUERY TOTAL ELEMENTS: {}", result.getTotalElements());

        log.error("PAGE QUERY CONTENT SIZE: {}", result.getContent().size());

        result.getContent().forEach(item -> {

            log.error("""
                    
                    PAGE RESULT
                    classCode: {}
                    facultyId: {}
                    subjectCode: {}
                    facultyName: {}
                    subjectDescription: {}
                    semester: {}
                    schoolYear: {}
                    """,
                    item.getClassCode(),
                    item.getFacultyId(),
                    item.getSubjectCode(),
                    item.getFacultyName(),
                    item.getSubjectDescription(),
                    item.getSemester(),
                    item.getSchoolYear()
            );
        });

        log.info("========== STUDENT LOAD DEBUG END ==========");

        return PageMapper.toPageResponse(result);
    }
}