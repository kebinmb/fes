package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.entities.legacy.LegacyStudent;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacyStudentRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentMigration extends BaseMigrationService {

    private final LegacyStudentRepository legacyStudentRepository;
    private final PrimaryStudentRepository primaryStudentRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 1000;

    public void migrate() {

        executePerCampus(database -> {

            log.info(
                    "Starting student migration : {}",
                    database.name()
            );

            List<LegacyStudent> data =
                    legacyStudentRepository.findAll();

            parallelMigrationExecutor.processInParallel(
                    data,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryStudent> toSave = batch.stream()
                                .filter(ls -> ls != null && ls.getId() != null)
                                .filter(ls ->
                                        !primaryStudentRepository
                                                .existsByLegacyDatabaseAndLegacyId(
                                                        database.name(),
                                                        ls.getId().getStudentId()
                                                )
                                )
                                .map(ls -> map(ls, database))
                                .toList();

                        if (!toSave.isEmpty()) {

                            primaryStudentRepository.saveAll(toSave);

                            log.info(
                                    "Saved {} student records from {}",
                                    toSave.size(),
                                    database.name()
                            );
                        }
                    });

            log.info(
                    "Student migration completed : {}",
                    database.name()
            );
        });
    }

    private PrimaryStudent map(
            LegacyStudent legacyStudent,
            LegacyDatabase database
    ) {

        PrimaryStudent primaryStudent = new PrimaryStudent();

        primaryStudent.setStudentId(
                MigrationIdGenerator.generateStringId(
                        database,
                        legacyStudent.getId().getStudentId()
                )
        );

        primaryStudent.setStudentLastname(
                legacyStudent.getId().getStudentLastname()
        );

        primaryStudent.setStudentFirstname(
                legacyStudent.getId().getStudentFirstname()
        );

        primaryStudent.setStudentMiddlename(
                legacyStudent.getStudentMiddlename()
        );

        primaryStudent.setCurriculumMajorId(
                legacyStudent.getCurriculumMajorId()
        );

        primaryStudent.setGender(
                legacyStudent.getGender()
        );

        // MIGRATION METADATA

        primaryStudent.setSourceCampus(database);

        primaryStudent.setLegacyDatabase(database.name());

        primaryStudent.setLegacyId(
                legacyStudent.getId().getStudentId()
        );

        return primaryStudent;
    }
}