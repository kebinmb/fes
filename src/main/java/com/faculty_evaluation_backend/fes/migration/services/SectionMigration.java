package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacySection;
import com.faculty_evaluation_backend.fes.entities.primary.PrimarySection;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacySectionRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionMigration extends BaseMigrationService {

    private final LegacySectionRepository legacySectionRepository;
    private final PrimarySectionRepository primarySectionRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 10;

    public void migrate() {

        executePerCampus(database -> {

            log.info("Starting section migration : {}", database.name());

            List<LegacySection> data =
                    legacySectionRepository.findAll();
            log.info(
                    "Fetched {} subject records from {}",
                    data.size(),
                    database.name()
            );
            parallelMigrationExecutor.processInParallel(
                    data,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimarySection> toSave = batch.stream()
                                .filter(ls -> ls != null && ls.getId() != null)
                                .filter(ls ->
                                        !primarySectionRepository
                                                .existsByLegacyDatabaseAndLegacyId(
                                                        database.name(),
                                                        String.valueOf(ls.getId().getSectionId())
                                                )
                                )
                                .map(ls -> map(ls, database))
                                .toList();

                        primarySectionRepository.saveAll(toSave);
                    });

            log.info("Section migration completed : {}", database.name());
        });
    }

    public PrimarySection map(
            LegacySection legacySection,
            LegacyDatabase database
    ) {

        PrimarySection primarySection = new PrimarySection();

        primarySection.setSectionId(
                MigrationIdGenerator.generateNumericId(
                        database,
                        legacySection.getId().getSectionId()
                )
        );

        primarySection.setProgramCode(
                legacySection.getId().getProgramCode()
        );

        primarySection.setSectionCode(
                legacySection.getSectionCode()
        );

        primarySection.setYearLevel(
                legacySection.getYearlevel()
        );

        primarySection.setSourceCampus(database);

        primarySection.setLegacyDatabase(database.name());

        primarySection.setLegacyId(
                String.valueOf(
                        legacySection.getId().getSectionId()
                )
        );

        return primarySection;
    }
}