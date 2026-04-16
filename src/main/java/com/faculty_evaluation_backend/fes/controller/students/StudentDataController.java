package com.faculty_evaluation_backend.fes.controller.students;

import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.dto.student.StudentEvaluationCheckResponse;
import com.faculty_evaluation_backend.fes.services.data.students.StudentService;
import com.faculty_evaluation_backend.fes.services.data.students.evaluation.StudentEvaluationService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentDataController {
    private final StudentService studentService;
    private final StudentEvaluationService studentEvaluationService;
    @GetMapping("/student-loads")
    public PageResponse<StudentClassLoadDTO> getStudentLoads(
            @RequestParam @NotBlank String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "primaryStudentLoadId,desc") String sort
    ){
        System.out.println("Student ID:" + studentId);
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

        return studentService.getStudentLoads(studentId, pageable);
    }

    @GetMapping("/check")
    public ResponseEntity<StudentEvaluationCheckResponse> checkEvaluationStatus(
            @RequestParam @NotBlank(message = "facultyId is required") String facultyId,
            @RequestParam @NotBlank(message = "evaluatorId is required") String evaluatorId,
            @RequestParam @NotBlank(message = "classCode is required") String classCode,
            @RequestParam @NotBlank(message = "semester is required") String semester,
            @RequestParam @NotNull(message = "schoolYear is required") Integer schoolYear
    ){
        boolean hasEvaluated = studentEvaluationService.hasEvaluatedFacultyForClass(
                facultyId, evaluatorId, classCode, semester, schoolYear
        );

        StudentEvaluationCheckResponse response = StudentEvaluationCheckResponse.builder()
                .hasEvaluated(hasEvaluated)
                .message(hasEvaluated
                        ? "You have already evaluated this faculty for this class"
                        : "You can submit an evaluation")
                .build();

        return ResponseEntity.ok(response);
    }

}
