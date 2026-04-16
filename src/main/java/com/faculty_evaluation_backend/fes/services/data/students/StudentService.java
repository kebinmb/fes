package com.faculty_evaluation_backend.fes.services.data.students;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.utilities.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    public PageResponse<StudentClassLoadDTO> getStudentLoads(
            String studentId,
            Pageable pageable
    ) {

        SchoolYearAndSemesterDTO data = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE);
        System.out.println("Active School Year and Semester: " + data);
        return PageMapper.toPageResponse(
                primaryStudentLoadRepository.findStudentLoadDTO(studentId, data.getSchoolYear(), data.getSemester(), pageable)
        );
    }
}
