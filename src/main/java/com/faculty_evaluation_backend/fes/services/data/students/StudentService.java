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
                data.getSemester()
        );

        Page<Object[]> result =
                primaryStudentLoadRepository.findStudentLoadDTO(
                        studentId,
                        data.getSchoolYear(),
                        data.getSemester().getValue(),
                        pageable
                );

        Page<StudentClassLoadDTO> mappedPage =
                result.map(row -> new StudentClassLoadDTO(
                        row[0] != null ? row[0].toString() : null,
                        row[1] != null ? row[1].toString() : null,
                        row[2] != null ? row[2].toString() : null,
                        row[3] != null ? ((Number) row[3]).intValue() : null,
                        row[4] != null ? row[4].toString() : null,
                        row[5] != null ? ((Number) row[5]).intValue() : null,
                        row[6] != null ? row[6].toString() : null,
                        row[7] != null ? row[7].toString() : null,
                        row[8] != null ? row[8].toString() : null,
                        row[9] != null ? row[9].toString() : null,
                        row[10] != null ? row[10].toString() : null
                ));

        return PageMapper.toPageResponse(mappedPage);
    }
}