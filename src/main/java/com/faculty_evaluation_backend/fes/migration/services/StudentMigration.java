package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.entities.legacy.LegacyClass;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudent;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyStudentRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentMigration {
    private final LegacyStudentRepository legacyStudentRepository;
    private final PrimaryStudentRepository  primaryStudentRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 1000;

    public void migrate(){
        log.info("Starting student migration.");
        List<LegacyStudent> data = legacyStudentRepository.findAll();
        parallelMigrationExecutor.processInParallel(data, BATCH_SIZE, batch -> {
            List<PrimaryStudent> toSave = batch.stream()
                    .filter(legacyStudent -> legacyStudent != null && legacyStudent.getId() != null)
                    .filter(legacyStudent -> !primaryStudentRepository.existsByStudentId(legacyStudent.getId().getStudentId()))
                    .map(this::map)
                    .toList();
            primaryStudentRepository.saveAll(toSave);
        });
    }

    private PrimaryStudent map(LegacyStudent legacyStudent){
        PrimaryStudent primaryStudent = new PrimaryStudent();
        primaryStudent.setStudentId(legacyStudent.getId().getStudentId());
        primaryStudent.setStudentLastname(legacyStudent.getId().getStudentLastname());
        primaryStudent.setStudentFirstname(legacyStudent.getId().getStudentFirstname());
        primaryStudent.setStudentMiddlename(legacyStudent.getStudentMiddlename());
        primaryStudent.setCurriculumMajorId(legacyStudent.getCurriculumMajorId());
        primaryStudent.setGender(legacyStudent.getGender());
        return primaryStudent;
    }
}
