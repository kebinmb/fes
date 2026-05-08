package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.dto.authentication.StudentAuthenticationDTO;
import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUser;
import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUserA;
import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUserB;
import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUserFT;
import com.faculty_evaluation_backend.fes.repositories.sis_authentication.StudentUserARepository;
import com.faculty_evaluation_backend.fes.repositories.sis_authentication.StudentUserBRepository;
import com.faculty_evaluation_backend.fes.repositories.sis_authentication.StudentUserFTRepository;
import com.faculty_evaluation_backend.fes.repositories.sis_authentication.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentAuthenticationLookUpService {

    private final StudentUserRepository studentUserRepository;
    private final StudentUserARepository studentUserARepository;
    private final StudentUserBRepository studentUserBRepository;
    private final StudentUserFTRepository studentUserFTRepository;

    public Optional<StudentAuthenticationDTO> findStudent(String studentId) {

        // Check student_user
        Optional<StudentUser> studentUser =
                studentUserRepository.findByStudentId(studentId);

        if (studentUser.isPresent()) {

            StudentUser student = studentUser.get();

            return Optional.of(
                    StudentAuthenticationDTO.builder()
                            .studentId(student.getStudentId())
                            .password(student.getPassword())
                            .email(student.getEmail())
                            .sourceTable("student_user")
                            .build()
            );
        }

        // Check student_user_a
        Optional<StudentUserA> studentUserA =
                studentUserARepository.findByStudentId(studentId);

        if (studentUserA.isPresent()) {

            StudentUserA student = studentUserA.get();

            return Optional.of(
                    StudentAuthenticationDTO.builder()
                            .studentId(student.getStudentId())
                            .password(student.getPassword())
                            .email(student.getEmail())
                            .sourceTable("student_user_a")
                            .build()
            );
        }

        // Check student_user_b
        Optional<StudentUserB> studentUserB =
                studentUserBRepository.findByStudentId(studentId);

        if (studentUserB.isPresent()) {

            StudentUserB student = studentUserB.get();

            return Optional.of(
                    StudentAuthenticationDTO.builder()
                            .studentId(student.getStudentId())
                            .password(student.getPassword())
                            .email(student.getEmail())
                            .sourceTable("student_user_b")
                            .build()
            );
        }

        // Check student_user_ft
        Optional<StudentUserFT> studentUserFT =
                studentUserFTRepository.findByStudentId(studentId);

        if (studentUserFT.isPresent()) {

            StudentUserFT student = studentUserFT.get();

            return Optional.of(
                    StudentAuthenticationDTO.builder()
                            .studentId(student.getStudentId())
                            .password(student.getPassword())
                            .email(student.getEmail())
                            .sourceTable("student_user_ft")
                            .build()
            );
        }

        return Optional.empty();
    }
}