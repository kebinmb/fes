package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacultyMigration extends BaseMigrationService {

    private final LegacyFacultyRepository legacyFacultyRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 10;

    public void migrate() {

        executePerCampus(database -> {

            log.info("Starting Faculty Migration : {}", database.name());

            List<LegacyFaculty> legacyList =
                    legacyFacultyRepository.findAll();
            log.info(
                    "Fetched {} subject records from {}",
                    legacyList.size(),
                    database.name()
            );
            parallelMigrationExecutor.processInParallel(
                    legacyList,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryFaculty> toSave = batch.stream()
                                .filter(l -> l != null && l.getId() != null)
                                .map(l -> map(l, database))
                                .toList();

                        if (!toSave.isEmpty()) {
                            primaryFacultyRepository.saveAll(toSave);
                        }
                    });

            log.info("Faculty Migration Completed : {}", database.name());
        });
    }

    private PrimaryFaculty map(
            LegacyFaculty legacyFaculty,
            LegacyDatabase database
    ) {

        PrimaryFaculty primaryFaculty = new PrimaryFaculty();

        primaryFaculty.setFacultyId(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacyFaculty.getId().getFacultyId()
                )
        );

        primaryFaculty.setFirstname(
                legacyFaculty.getId().getFirstname()
        );

        primaryFaculty.setLastname(
                legacyFaculty.getId().getLastname()
        );

        primaryFaculty.setMiddlename(
                legacyFaculty.getMiddlename()
        );

        primaryFaculty.setPosition(
                legacyFaculty.getPosition()
        );

        primaryFaculty.setLoadLimit(
                legacyFaculty.getLoadLimit()
        );

        primaryFaculty.setCollege(College.FOR_MIGRATION);

        primaryFaculty.setStatus(Status.INACTIVE);

        // IMPORTANT
        primaryFaculty.setSourceCampus(database);

        primaryFaculty.setLegacyDatabase(database.name());

        primaryFaculty.setLegacyId(
                legacyFaculty.getId().getFacultyId()
        );

        return primaryFaculty;
    }
}