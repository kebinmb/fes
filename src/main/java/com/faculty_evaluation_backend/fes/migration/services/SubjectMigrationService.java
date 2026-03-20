package com.faculty_evaluation_backend.fes.migration.services;

import com.faculty_evaluation_backend.fes.entities.legacy.LegacySubject;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.entities.primary.PrimarySubject;
import com.faculty_evaluation_backend.fes.migration.engine.ParallelMigrationExecutor;
import com.faculty_evaluation_backend.fes.repositories.legacy.LegacySubjectRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectMigrationService {
    private final LegacySubjectRepository legacySubjectRepository;
    private final PrimarySubjectRepository primarySubjectRepository;
    private final ParallelMigrationExecutor parallelMigrationExecutor;

    private static final int BATCH_SIZE = 1000;

    public void migrate(){
        List<LegacySubject> data = legacySubjectRepository.findAll();
        parallelMigrationExecutor.processInParallel(data,BATCH_SIZE,batch -> {
            List<PrimarySubject> toSave = batch.stream()
                    .filter(legacySubject -> legacySubject != null && legacySubject.getId() != null)
                    .filter(legacySubject -> !primarySubjectRepository.existsBySubjectCodeAndDescriptiveTitle(
                            legacySubject.getId().getSubjectCode(),
                            legacySubject.getId().getDescriptiveTitle()
                    ))
                    .map(this::map)
                    .toList();
            primarySubjectRepository.saveAll(toSave);
        });
    }

    private PrimarySubject map(LegacySubject legacySubject){
        PrimarySubject primarySubject = new PrimarySubject();
        primarySubject.setSubjectCode(legacySubject.getId().getSubjectCode());
        primarySubject.setDescriptiveTitle(legacySubject.getId().getDescriptiveTitle());
        return primarySubject;
    }
}
