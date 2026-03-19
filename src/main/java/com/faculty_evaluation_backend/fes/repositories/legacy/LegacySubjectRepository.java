package com.faculty_evaluation_backend.fes.repositories.legacy;


import com.faculty_evaluation_backend.fes.entities.legacy.LegacySubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LegacySubjectRepository extends JpaRepository<LegacySubject,Long> {
}
