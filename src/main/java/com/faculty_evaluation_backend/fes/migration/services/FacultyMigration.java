package com.faculty_evaluation_backend.fes.migration.services;

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
public class FacultyMigration {
    private final LegacyFacultyRepository legacyFacultyRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 1000;

    public void migrate(){
        log.info("Starting Faculty Migration.");
        List<LegacyFaculty> legacyList = legacyFacultyRepository.findAll();
        parallelMigrationExecutor.processInParallel(legacyList,BATCH_SIZE,batch -> {
            List<PrimaryFaculty> toSave = batch.stream()
                    .filter(l -> l != null && l.getId() != null)
                    .filter(l -> !primaryFacultyRepository.existsByFacultyId(l.getId().getFacultyId()))
                    .map(this::map)
                    .toList();
            if(!toSave.isEmpty()){
                primaryFacultyRepository.saveAll(toSave);
            }
        });
        log.info("Faculty Migration Completed");
    }

    private PrimaryFaculty map(LegacyFaculty legacyFaculty){
        PrimaryFaculty primaryFaculty = new PrimaryFaculty();
        primaryFaculty.setFacultyId(legacyFaculty.getId().getFacultyId());
        primaryFaculty.setFirstname(legacyFaculty.getId().getFirstname());
        primaryFaculty.setLastname(legacyFaculty.getId().getLastname());
        primaryFaculty.setMiddlename(legacyFaculty.getMiddlename());
        primaryFaculty.setPosition(legacyFaculty.getPosition());
        primaryFaculty.setLoadLimit(legacyFaculty.getLoadLimit());
        primaryFaculty.setCollege(College.FOR_MIGRATION);
        primaryFaculty.setStatus(Status.INACTIVE);
        return primaryFaculty;
    }
}
