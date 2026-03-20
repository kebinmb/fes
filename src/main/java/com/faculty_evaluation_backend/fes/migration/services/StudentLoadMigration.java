package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudentLoad;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLoadMigration {
    private final LegacyStudentLoadRepository legacyStudentLoadRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 1000;

    public void migrate(){
        List<LegacyStudentLoad> data = legacyStudentLoadRepository.findAll();
        parallelMigrationExecutor.processInParallel(data,BATCH_SIZE,batch -> {
            List<PrimaryStudentLoad> toSave = batch.stream()
                    .filter(legacyStudentLoad -> legacyStudentLoad != null && legacyStudentLoad.getId() != null)
                    .filter(legacyStudentLoad -> !primaryStudentLoadRepository.existsByLoadIdAndStudentIdAndYearLevelAndClassCode(
                            legacyStudentLoad.getId().getLoadId(),
                            legacyStudentLoad.getId().getStudentId(),
                            legacyStudentLoad.getId().getYearLevel(),
                            legacyStudentLoad.getId().getClassCode()
                    ))
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
        primaryStudentLoad.setClassCode(legacyStudentLoad.getId().getClassCode());
        return primaryStudentLoad;
    }
}
