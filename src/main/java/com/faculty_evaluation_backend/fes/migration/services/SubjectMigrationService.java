package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacySubject;
import com.faculty_evaluation_backend.fes.entities.primary.PrimarySubject;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacySubjectRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectMigrationService extends BaseMigrationService {

    private final LegacySubjectRepository legacySubjectRepository;
    private final PrimarySubjectRepository primarySubjectRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 500;

    public void migrate() {

        executePerCampus(database -> {

            log.info(
                    "Starting subject migration : {}",
                    database.name()
            );

            List<LegacySubject> data =
                    legacySubjectRepository.findAll();

            log.info(
                    "Fetched {} subject records from {}",
                    data.size(),
                    database.name()
            );

            Set<String> existingLegacyIds =
                    Set.copyOf(
                            primarySubjectRepository
                                    .findLegacyIdsByDatabase(database.name())
                    );

            parallelMigrationExecutor.processInParallel(
                    data,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimarySubject> toSave = batch.stream()
                                .filter(ls -> ls != null && ls.getId() != null)
                                .filter(ls ->
                                        !existingLegacyIds.contains(
                                                ls.getId().getSubjectCode()
                                        )
                                )
                                .map(ls -> map(ls, database))
                                .toList();

                        if (!toSave.isEmpty()) {

                            primarySubjectRepository.saveAll(toSave);

                            log.info(
                                    "Saved {} subject records from {}",
                                    toSave.size(),
                                    database.name()
                            );
                        }
                    });

            log.info(
                    "Subject migration completed : {}",
                    database.name()
            );
        });
    }

    private PrimarySubject map(
            LegacySubject legacySubject,
            LegacyDatabase database
    ) {

        PrimarySubject primarySubject = new PrimarySubject();

        primarySubject.setSubjectCode(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacySubject.getId().getSubjectCode()
                )
        );

        primarySubject.setDescriptiveTitle(
                legacySubject.getId().getDescriptiveTitle()
        );

        // MIGRATION METADATA

        primarySubject.setSourceCampus(database);

        primarySubject.setLegacyDatabase(database.name());

        primarySubject.setLegacyId(
                legacySubject.getId().getSubjectCode()
        );

        return primarySubject;
    }
}
