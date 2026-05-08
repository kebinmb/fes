package com.faculty_evaluation_backend.fes.repositories.sis_authentication;

import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUser;
import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUserFT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentUserFTRepository extends JpaRepository<StudentUserFT, Integer> {
    Optional<StudentUserFT> findByStudentId(String studentId);
}
