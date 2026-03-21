package com.faculty_evaluation_backend.fes.migration.services;

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
public class SectionMigration {
    private final LegacySectionRepository legacySectionRepository;
    private final PrimarySectionRepository primarySectionRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 1000;

    public void migrate(){
        log.info("Starting section migration.");
        List<LegacySection> data = legacySectionRepository.findAll();
        parallelMigrationExecutor.processInParallel(data,BATCH_SIZE,batch -> {
            List<PrimarySection> toSave = batch.stream()
                    .filter(legacySection -> legacySection != null && legacySection.getId() != null)
                    .filter(legacySection -> !primarySectionRepository.existsBySectionIdAndProgramCodeAndSectionCodeAndYearLevel(
                            legacySection.getId().getSectionId(),
                            legacySection.getId().getProgramCode(),
                            legacySection.getSectionCode(),
                            legacySection.getYearlevel()
                    ))
                    .map(this::map)
                    .toList();
            primarySectionRepository.saveAll(toSave);
        });
    }

    public PrimarySection map(LegacySection legacySection){
        PrimarySection primarySection = new PrimarySection();
        primarySection.setSectionId(legacySection.getId().getSectionId());
        primarySection.setProgramCode(legacySection.getId().getProgramCode());
        primarySection.setSectionCode(legacySection.getSectionCode());
        primarySection.setYearLevel(legacySection.getYearlevel());
        return primarySection;
    }
}
