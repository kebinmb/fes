package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudentLoad;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
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
public class StudentLoadMigration extends BaseMigrationService {

    private final LegacyStudentLoadRepository legacyStudentLoadRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;
    private final PrimaryClassRepository primaryClassRepository;

    private static final int BATCH_SIZE = 10;

    public void migrate() {

        executePerCampus(database -> {

            log.info(
                    "Starting Student Load Migration : {}",
                    database.name()
            );

            List<LegacyStudentLoad> data =
                    legacyStudentLoadRepository.findAll();
            log.info(
                    "Fetched {} subject records from {}",
                    data.size(),
                    database.name()
            );
            Set<String> validClassCodes =
                    primaryClassRepository.findAll()
                            .stream()
                            .filter(pc ->
                                    database.name().equals(
                                            pc.getLegacyDatabase()
                                    )
                            )
                            .map(PrimaryClass::getLegacyId)
                            .collect(Collectors.toSet());

            parallelMigrationExecutor.processInParallel(
                    data,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryStudentLoad> toSave = batch.stream()
                                .filter(ls -> ls != null && ls.getId() != null)
                                .filter(ls ->
                                        validClassCodes.contains(
                                                ls.getId()
                                                        .getClassCode()
                                                        .toString()
                                        )
                                )
                                .filter(ls ->
                                        !primaryStudentLoadRepository
                                                .existsByLegacyDatabaseAndLegacyId(
                                                        database.name(),
                                                        ls.getId()
                                                                .getLoadId()
                                                                .toString()
                                                )
                                )
                                .map(ls -> map(ls, database))
                                .toList();

                        if (!toSave.isEmpty()) {

                            primaryStudentLoadRepository.saveAll(toSave);

                            log.info(
                                    "Saved {} student load records from {}",
                                    toSave.size(),
                                    database.name()
                            );
                        }
                    });

            log.info(
                    "Student Load Migration Completed : {}",
                    database.name()
            );
        });
    }

    private PrimaryStudentLoad map(
            LegacyStudentLoad legacyStudentLoad,
            LegacyDatabase database
    ) {

        PrimaryStudentLoad primaryStudentLoad =
                new PrimaryStudentLoad();

        primaryStudentLoad.setStudentId(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacyStudentLoad.getId().getStudentId()
                )
        );

        primaryStudentLoad.setLoadId(
                legacyStudentLoad.getId().getLoadId()
        );

        primaryStudentLoad.setYearLevel(
                legacyStudentLoad.getId().getYearLevel()
        );

        primaryStudentLoad.setClassCode(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacyStudentLoad.getId()
                                .getClassCode()
                                .toString()
                )
        );

        // MIGRATION METADATA

        primaryStudentLoad.setSourceCampus(database);

        primaryStudentLoad.setLegacyDatabase(database.name());

        primaryStudentLoad.setLegacyId(
                legacyStudentLoad.getId()
                        .getLoadId()
                        .toString()
        );

        return primaryStudentLoad;
    }
}