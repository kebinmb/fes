package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudentLoad;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import com.faculty_evaluation_backend.fes.utilities.normalization.SemesterNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLoadMigration extends BaseMigrationService {

    private final LegacyStudentLoadRepository legacyStudentLoadRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;
    private final PrimaryClassRepository primaryClassRepository;
    private final PrimaryStudentRepository primaryStudentRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    private static final int BATCH_SIZE = 500;

    public void migrate() {

        executePerCampus(database -> {

            log.info(
                    "Starting Student Load Migration : {}",
                    database.name()
            );

            SchoolYearAndSemester filterData =
                    schoolYearAndSemesterRepository
                            .findByStatus(Status.ACTIVE)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "No active school year and semester found."
                                    )
                            );

            String canonicalSemester =
                    SemesterNormalizer.toCanonicalValue(
                            filterData.getSemester().getValue()
                    );
            Integer activeSchoolYear = filterData.getSchoolYear();

            List<LegacyStudentLoad> data =
                    legacyStudentLoadRepository.findAll();
            log.info(
                    "Fetched {} student load records from {}",
                    data.size(),
                    database.name()
            );

            // Only consider class codes that belong to the active term in primary_class
            Set<String> validClassCodes =
                    primaryClassRepository
                            .findClassCodesByDatabaseAndTerm(
                                    database.name(),
                                    activeSchoolYear,
                                    canonicalSemester
                            );

            Set<String> validStudentIds =
                    primaryStudentRepository
                            .findStudentIdsByLegacyDatabase(database.name());

            Set<String> existingLegacyIds =
                    primaryStudentLoadRepository
                            .findLegacyIdsByDatabase(database.name());

            parallelMigrationExecutor.processInParallel(
                    data,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryStudentLoad> toSave = batch.stream()
                                .filter(ls -> ls != null && ls.getId() != null)
                                .filter(ls -> {
                                    if (ls.getId().getClassCode() == null) {
                                        return false;
                                    }
                                    String generatedClassCode =
                                            MigrationIdGenerator.generateStringId(
                                                    database,
                                                    ls.getId().getClassCode().toString()
                                            );
                                    return validClassCodes.contains(generatedClassCode);
                                })
                                .filter(ls ->
                                        !existingLegacyIds.contains(
                                                ls.getId()
                                                        .getLoadId()
                                                        .toString()
                                        )
                                )
                                .filter(ls ->
                                        validStudentIds.contains(
                                                MigrationIdGenerator.generateStringId(
                                                        database,
                                                        ls.getId().getStudentId()
                                                )
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
                MigrationIdGenerator.generateNumericId(
                        database,
                        legacyStudentLoad.getId().getLoadId()
                )
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
