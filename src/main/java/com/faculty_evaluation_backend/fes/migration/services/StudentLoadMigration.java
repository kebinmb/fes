package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudentLoad;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLoadMigration {
    private final LegacyStudentLoadRepository legacyStudentLoadRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;
    private final PrimaryClassRepository  primaryClassRepository;
    private static final int BATCH_SIZE = 1000;

    public void migrate() {
        log.info("Starting Student Load Migration");

        List<LegacyStudentLoad> data = legacyStudentLoadRepository.findAll();
        Set<String> validClassCodes = primaryClassRepository.findAll()
                .stream()
                .map(PrimaryClass::getClassCode)
                .collect(Collectors.toSet());
        parallelMigrationExecutor.processInParallel(data, BATCH_SIZE, batch -> {
            List<PrimaryStudentLoad> toSave = batch.stream()
                    .filter(ls -> ls != null && ls.getId() != null)
                    .filter(ls -> validClassCodes.contains(ls.getId().getClassCode().toString()))
                    .map(this::map)
                    .toList();
            primaryStudentLoadRepository.saveAll(toSave);
        });
    }

    private PrimaryStudentLoad map(LegacyStudentLoad legacyStudentLoad){
        PrimaryStudentLoad primaryStudentLoad = new PrimaryStudentLoad();
        primaryStudentLoad.setStudentId(legacyStudentLoad.getId().getStudentId());
        primaryStudentLoad.setLoadId(legacyStudentLoad.getId().getLoadId());
        primaryStudentLoad.setYearLevel(legacyStudentLoad.getId().getYearLevel());
        primaryStudentLoad.setClassCode(legacyStudentLoad.getId().getClassCode().toString());
        return primaryStudentLoad;
    }
}
