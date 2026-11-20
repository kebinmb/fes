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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacultyMigration extends BaseMigrationService {

    private final LegacyFacultyRepository legacyFacultyRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 500;

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

            Set<String> existingLegacyIds =
                    primaryFacultyRepository
                            .findLegacyIdsByDatabase(database.name());

            parallelMigrationExecutor.processInParallel(
                    legacyList,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryFaculty> toSave = batch.stream()
                                .filter(l -> l != null && l.getId() != null)
                                .filter(l ->
                                        !existingLegacyIds.contains(
                                                l.getId().getFacultyId()
                                        )
                                )
                                .map(l -> map(l, database))
                                .toList();

                        if (!toSave.isEmpty()) {
                            primaryFacultyRepository.saveAll(toSave);
                        }
                    });

            log.info("Faculty Migration Completed : {}", database.name());
        });

        syncFacultyColleges();
    }

    public void syncFacultyColleges() {
        log.info("Starting post-migration Faculty College Mapping sync...");
        int totalUpdated = 0;
        for (var entry : FacultyCollegeMapping.getFacultyIdsByCollege().entrySet()) {
            College college = entry.getKey();
            List<String> facultyIds = entry.getValue();
            if (!facultyIds.isEmpty()) {
                int updated = primaryFacultyRepository.updateCollegeByFacultyIds(college, facultyIds);
                log.info("Updated {} faculty records to college {}", updated, college);
                totalUpdated += updated;
            }
        }
        log.info("Finished Faculty College Mapping sync. Total updated: {}", totalUpdated);
    }

    private PrimaryFaculty map(
            LegacyFaculty legacyFaculty,
            LegacyDatabase database
    ) {

        PrimaryFaculty primaryFaculty = new PrimaryFaculty();

        String facultyId = MigrationIdGenerator.generateStringId(
                database,
                legacyFaculty.getId().getFacultyId()
        );

        primaryFaculty.setFacultyId(facultyId);

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

        primaryFaculty.setCollege(FacultyCollegeMapping.getCollege(facultyId));

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
