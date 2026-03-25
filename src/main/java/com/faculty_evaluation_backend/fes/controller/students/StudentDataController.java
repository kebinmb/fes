package com.faculty_evaluation_backend.fes.controller.students;

import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.services.data.students.StudentService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentDataController {
    private final StudentService studentService;

    @GetMapping("/student-loads")
    public PageResponse<StudentClassLoadDTO> getStudentLoads(
            @RequestParam @NotBlank String studentId,
            @RequestParam @NotBlank String yearLevel,
            @RequestParam @NotNull Integer schoolYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "primaryStudentLoadId,desc") String sort
    ){
        String sortField = "primaryStudentLoadId";
        Sort.Direction direction = Sort.Direction.DESC;

        if(sort.contains(",")){
            String[] parts = sort.split(",");
            sortField = parts[0];
            direction = Sort.Direction.fromString(parts[1]);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortField)
        );

        return studentService.getStudentLoads(studentId, yearLevel, schoolYear, pageable);
    }

}
