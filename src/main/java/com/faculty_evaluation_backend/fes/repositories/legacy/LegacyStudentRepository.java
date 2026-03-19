package com.faculty_evaluation_backend.fes.repositories.legacy;


import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacyStudentId;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegacyStudentRepository extends JpaRepository<LegacyStudent, LegacyStudentId> {
}
