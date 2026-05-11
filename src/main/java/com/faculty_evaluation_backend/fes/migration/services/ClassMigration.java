package com.faculty_evaluation_backend.fes.migration.services;

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
public class ClassMigration {

    private final LegacyClassRepository legacyClassRepository;
    private final PrimaryClassRepository primaryClassRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    private static final int BATCH_SIZE = 1000;

    public void migrate() {
        log.info("Starting Class Migration");
        SchoolYearAndSemester filterData = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new RuntimeException("No active school year and semester found."));
        List<LegacyClass> data = legacyClassRepository.findBySemesterAndSchoolYear(filterData.getSemester().getValue(), filterData.getSchoolYear());

        log.info("Found {} legacy class records for migration.", data.size());
        parallelMigrationExecutor.processInParallel(data, BATCH_SIZE, batch -> {

            List<PrimaryClass> toSave = batch.stream().filter(lc -> lc != null && lc.getId() != null).map(this::map).toList();

            try {

                primaryClassRepository.saveAll(toSave);

                log.info("Saved {} class records.", toSave.size());

            } catch (Exception e) {

                log.warn("Batch failed due to duplicates or constraint issues", e);
            }
        });

        log.info("Class Migration Completed");
    }

    private PrimaryClass map(LegacyClass legacyClass) {

        PrimaryClass primaryClass = new PrimaryClass();

        primaryClass.setClassCode(legacyClass.getId().getClassCode().toString());

        primaryClass.setFacultyId(legacyClass.getFacultyId());

        primaryClass.setSubjectCode(legacyClass.getSubjectCode());

        primaryClass.setSectionId(legacyClass.getId().getSectionId());

        primaryClass.setSemester(legacyClass.getId().getSemester());

        primaryClass.setSchoolYear(legacyClass.getId().getSchoolYear());

        return primaryClass;
    }
}