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
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.utilities.normalization.SemesterNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassMigration extends BaseMigrationService {

    private static final int BATCH_SIZE = 500;
    private final LegacyClassRepository legacyClassRepository;
    private final PrimaryClassRepository primaryClassRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
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

            String canonicalSemester =
                    SemesterNormalizer.toCanonicalValue(
                            filterData.getSemester().getValue()
                    );
            Integer activeSchoolYear = filterData.getSchoolYear();

            List<LegacyClass> data =
                    legacyClassRepository.findBySemesterAndSchoolYear(
                            filterData.getSemester().getValue(),
                            activeSchoolYear
                    );

            log.info(
                    "Found {} legacy class records for migration from {}",
                    data.size(),
                    database.name()
            );

            // Fetch existing classes already in primary_class for this campus and active term
            List<PrimaryClass> existingClasses =
                    primaryClassRepository
                            .findByLegacyDatabaseAndSchoolYearAndSemester(
                                    database.name(),
                                    activeSchoolYear,
                                    canonicalSemester
                            );

            ConcurrentMap<String, PrimaryClass> existingByLegacyId = new ConcurrentHashMap<>();
            for (PrimaryClass pc : existingClasses) {
                if (pc.getLegacyId() != null) {
                    existingByLegacyId.put(pc.getLegacyId(), pc);
                }
                // Also index by raw class code for backwards compatibility before V57 migration
                if (pc.getClassCode() != null) {
                    String raw = pc.getClassCode().contains("-")
                            ? pc.getClassCode().substring(pc.getClassCode().indexOf("-") + 1)
                            : pc.getClassCode();
                    existingByLegacyId.putIfAbsent(raw, pc);
                }
            }

            Set<String> validFacultyIds =
                    primaryFacultyRepository
                            .findFacultyIdsByLegacyDatabase(database.name());

            // Pre-deduplicate by term-scoped legacyId so parallel batches never race on the same key
            Map<String, LegacyClass> deduplicatedData = new LinkedHashMap<>();
            for (LegacyClass lc : data) {
                if (lc != null && lc.getId() != null && lc.getId().getClassCode() != null) {
                    String legacyId = MigrationIdGenerator.generateClassLegacyId(
                            lc.getId().getClassCode(),
                            lc.getId().getSchoolYear() != null ? lc.getId().getSchoolYear() : activeSchoolYear,
                            lc.getId().getSemester() != null ? lc.getId().getSemester() : canonicalSemester
                    );
                    deduplicatedData.put(legacyId, lc);
                }
            }

            List<LegacyClass> uniqueData = new ArrayList<>(deduplicatedData.values());

            parallelMigrationExecutor.processInParallel(
                    uniqueData,
                    BATCH_SIZE,
                    batch -> {

                        List<PrimaryClass> toSave = new ArrayList<>();

                        for (LegacyClass lc : batch) {
                            String legacyId = MigrationIdGenerator.generateClassLegacyId(
                                    lc.getId().getClassCode(),
                                    lc.getId().getSchoolYear() != null ? lc.getId().getSchoolYear() : activeSchoolYear,
                                    lc.getId().getSemester() != null ? lc.getId().getSemester() : canonicalSemester
                            );

                            String generatedFacultyId = null;
                            if (lc.getFacultyId() != null && !lc.getFacultyId().trim().isEmpty()) {
                                String facId =
                                        MigrationIdGenerator.generateStringId(
                                                database,
                                                lc.getFacultyId().trim()
                                        );
                                if (!validFacultyIds.contains(facId)) {
                                    // Skip classes whose assigned faculty does not exist in primary_faculty
                                    continue;
                                }
                                generatedFacultyId = facId;
                            }

                            PrimaryClass existing = existingByLegacyId.get(legacyId);
                            if (existing == null) {
                                existing = existingByLegacyId.get(lc.getId().getClassCode().toString());
                            }

                            if (existing != null) {
                                // In-place update (UPSERT) for classes in the active semester
                                boolean updated = false;

                                if (!Objects.equals(existing.getFacultyId(), generatedFacultyId)) {
                                    existing.setFacultyId(generatedFacultyId);
                                    updated = true;
                                }

                                String newSubjectCode =
                                        MigrationIdGenerator.generateStringId(
                                                database,
                                                lc.getSubjectCode()
                                        );
                                if (!Objects.equals(existing.getSubjectCode(), newSubjectCode)) {
                                    existing.setSubjectCode(newSubjectCode);
                                    updated = true;
                                }

                                Integer newSectionId =
                                        MigrationIdGenerator.generateNumericId(
                                                database,
                                                lc.getId().getSectionId()
                                        );
                                if (!Objects.equals(existing.getSectionId(), newSectionId)) {
                                    existing.setSectionId(newSectionId);
                                    updated = true;
                                }

                                if (!legacyId.equals(existing.getLegacyId())) {
                                    existing.setLegacyId(legacyId);
                                    updated = true;
                                }

                                if (updated) {
                                    toSave.add(existing);
                                }
                            } else {
                                // Insert fresh record for this term
                                PrimaryClass newClass = map(
                                        lc,
                                        database,
                                        legacyId,
                                        generatedFacultyId,
                                        activeSchoolYear,
                                        canonicalSemester
                                );
                                existingByLegacyId.put(legacyId, newClass);
                                toSave.add(newClass);
                            }
                        }

                        if (!toSave.isEmpty()) {

                            primaryClassRepository.saveAll(toSave);

                            log.info(
                                    "Saved/Updated {} class records from {}",
                                    toSave.size(),
                                    database.name()
                            );
                        }
                    });

            int activatedFacultyCount =
                    primaryFacultyRepository
                            .activateFacultyWithClassesByDatabase(database.name());

            log.info(
                    "Activated {} faculty records with class assignments for {}",
                    activatedFacultyCount,
                    database.name()
            );

            log.info(
                    "Class Migration Completed : {}",
                    database.name()
            );
        });
    }

    private PrimaryClass map(
            LegacyClass legacyClass,
            LegacyDatabase database,
            String legacyId,
            String generatedFacultyId,
            Integer activeSchoolYear,
            String canonicalSemester
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

        primaryClass.setFacultyId(generatedFacultyId);

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
                legacyClass.getId().getSemester() != null
                        ? legacyClass.getId().getSemester()
                        : canonicalSemester
        );

        primaryClass.setSchoolYear(
                legacyClass.getId().getSchoolYear() != null
                        ? legacyClass.getId().getSchoolYear()
                        : activeSchoolYear
        );

        // MIGRATION METADATA

        primaryClass.setSourceCampus(database);

        primaryClass.setLegacyDatabase(database.name());

        primaryClass.setLegacyId(legacyId);

        return primaryClass;
    }
}
