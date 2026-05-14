package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyClass;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassMigration extends BaseMigrationService {

    private static final int BATCH_SIZE = 10;
    private final LegacyClassRepository legacyClassRepository;
    private final PrimaryClassRepository primaryClassRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;

    public void migrate() {

        executePerCampus(database -> {

            log.info("Starting Class Migration : {}", database.name());

            SchoolYearAndSemester filterData =
                    schoolYearAndSemesterRepository
                            .findByStatus(Status.ACTIVE)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "No active school year and semester found."
                                    )
                            );

            List<LegacyClass> data =
                    legacyClassRepository.findBySemesterAndSchoolYear(
                            filterData.getSemester().getValue(),
                            filterData.getSchoolYear()
                    );

            log.info(
                    "Found {} legacy class records for migration from {}",
                    data.size(),
                    database.name()
            );

            parallelMigrationExecutor.processInParallel(
                    data,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryClass> toSave = batch.stream()
                                .filter(lc -> lc != null && lc.getId() != null)
                                .filter(lc ->
                                        !primaryClassRepository
                                                .existsByLegacyDatabaseAndLegacyId(
                                                        database.name(),
                                                        lc.getId()
                                                                .getClassCode()
                                                                .toString()
                                                )
                                )
                                .map(lc -> map(lc, database))
                                .toList();

                        if (!toSave.isEmpty()) {

                            primaryClassRepository.saveAll(toSave);

                            log.info(
                                    "Saved {} class records from {}",
                                    toSave.size(),
                                    database.name()
                            );
                        }
                    });

            log.info(
                    "Class Migration Completed : {}",
                    database.name()
            );
        });
    }

    private PrimaryClass map(
            LegacyClass legacyClass,
            LegacyDatabase database
    ) {

        PrimaryClass primaryClass = new PrimaryClass();

        primaryClass.setClassCode(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacyClass.getId()
                                .getClassCode()
                                .toString()
                )
        );

        primaryClass.setFacultyId(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacyClass.getFacultyId()
                )
        );

        primaryClass.setSubjectCode(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacyClass.getSubjectCode()
                )
        );

        primaryClass.setSectionId(
                MigrationIdGenerator.generateNumericId(
                        database,
                        legacyClass.getId().getSectionId()
                )
        );

        primaryClass.setSemester(
                legacyClass.getId().getSemester()
        );

        primaryClass.setSchoolYear(
                legacyClass.getId().getSchoolYear()
        );

        // MIGRATION METADATA

        primaryClass.setSourceCampus(database);

        primaryClass.setLegacyDatabase(database.name());

        primaryClass.setLegacyId(
                legacyClass.getId()
                        .getClassCode()
                        .toString()
        );

        return primaryClass;
    }
}