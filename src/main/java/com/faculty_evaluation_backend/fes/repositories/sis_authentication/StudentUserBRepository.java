package com.faculty_evaluation_backend.fes.repositories.sis_authentication;

import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUser;
import com.faculty_evaluation_backend.fes.entities.sis_authentication.StudentUserB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentUserBRepository extends JpaRepository<StudentUserB,Integer> {
    Optional<StudentUserB> findByStudentId(String studentId);
}
