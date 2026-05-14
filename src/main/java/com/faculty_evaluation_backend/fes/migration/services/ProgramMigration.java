package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
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
public class ProgramMigration extends BaseMigrationService {

    private final LegacyProgramRepository legacyProgramRepository;
    private final PrimaryProgramRepository primaryProgramRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 10;

    public void migrate() {

        executePerCampus(database -> {

            log.info("Starting program migration : {}", database.name());

            List<LegacyProgram> data =
                    legacyProgramRepository.findAll();
            log.info(
                    "Fetched {} subject records from {}",
                    data.size(),
                    database.name()
            );
            parallelMigrationExecutor.processInParallel(
                    data,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryProgram> toSave = batch.stream()
                                .filter(lp -> lp != null && lp.getId() != null)
                                .map(lp -> map(lp, database))
                                .toList();

                        primaryProgramRepository.saveAll(toSave);
                    });

            log.info("Program migration completed : {}", database.name());
        });
    }

    private PrimaryProgram map(
            LegacyProgram legacyProgram,
            LegacyDatabase database
    ) {

        PrimaryProgram primaryProgram = new PrimaryProgram();

        primaryProgram.setProgramCode(
                legacyProgram.getId().getProgramCode()
        );

        primaryProgram.setProgramTitle(
                legacyProgram.getId().getProgramTitle()
        );

        primaryProgram.setCollegeCode(
                legacyProgram.getCollegeCode()
        );

        primaryProgram.setYearGranted(
                legacyProgram.getYearGranted()
        );

        primaryProgram.setSourceCampus(database);

        primaryProgram.setLegacyDatabase(database.name());

        primaryProgram.setLegacyId(
                legacyProgram.getId().getProgramCode()
        );

        return primaryProgram;
    }
}