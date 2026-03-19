package com.faculty_evaluation_backend.fes.repositories.legacy;

import com.faculty_evaluation_backend.fes.entities.legacy.LegacyProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegacyProgramRepository extends JpaRepository<LegacyProgram,Long> {
}
