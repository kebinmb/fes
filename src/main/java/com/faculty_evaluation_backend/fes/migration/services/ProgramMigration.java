package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.entities.legacy.LegacyProgram;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryProgram;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyProgramRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramMigration {
    private final LegacyProgramRepository legacyProgramRepository;
    private final PrimaryProgramRepository primaryProgramRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 1000;

    public void migrate(){
        log.info("Starting program migration");
        List<LegacyProgram> data =  legacyProgramRepository.findAll();
        parallelMigrationExecutor.processInParallel(data, BATCH_SIZE, batch -> {
            List<PrimaryProgram> toSave = batch.stream()
                    .filter(legacyProgram -> legacyProgram != null && legacyProgram.getId() != null)
                    .filter(legacyProgram -> !primaryProgramRepository.existsByProgramCodeAndProgramTitleAndYearGrantedAndCollegeCode(
                            legacyProgram.getId().getProgramCode(),
                            legacyProgram.getId().getProgramTitle(),
                            legacyProgram.getYearGranted(),
                            legacyProgram.getCollegeCode()
                    ))
                    .map(this::map)
                    .toList();
            primaryProgramRepository.saveAll(toSave);
        });
    }

    private PrimaryProgram map(LegacyProgram legacyProgram){
        PrimaryProgram primaryProgram = new PrimaryProgram();
        primaryProgram.setProgramCode(legacyProgram.getCollegeCode());
        primaryProgram.setProgramTitle(legacyProgram.getId().getProgramTitle());
        primaryProgram.setCollegeCode(legacyProgram.getCollegeCode());
        primaryProgram.setYearGranted(legacyProgram.getYearGranted());
        return primaryProgram;
    }
}
