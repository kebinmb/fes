package com.faculty_evaluation_backend.fes.services.data.students;

import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.utilities.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;

    public PageResponse<StudentClassLoadDTO> getStudentLoads(
            String studentId,
            String yearLevel,
            Integer schoolYear,
            Pageable pageable
    ) {
        return PageMapper.toPageResponse(
                primaryStudentLoadRepository.findStudentLoadDTO(studentId, yearLevel, schoolYear, pageable)
        );
    }
}
