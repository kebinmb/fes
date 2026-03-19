package com.faculty_evaluation_backend.fes.repositories.authentication;


import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentAccountsRepository  extends JpaRepository<StudentAccounts, Long> {
    Optional<StudentAccounts> findByStudentId(String studentId);

    Optional<StudentAccounts> findByEmail(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);
}
