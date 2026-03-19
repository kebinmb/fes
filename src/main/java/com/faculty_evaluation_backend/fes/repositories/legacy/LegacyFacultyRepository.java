package com.faculty_evaluation_backend.fes.repositories.legacy;


import com.faculty_evaluation_backend.fes.entities.legacy.LegacyFaculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegacyFacultyRepository extends JpaRepository<LegacyFaculty,Long> {
}
