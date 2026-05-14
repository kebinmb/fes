package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.entities.primary.PrimarySubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrimarySubjectRepository extends JpaRepository<PrimarySubject, Long> {
    Optional<PrimarySubject> findBySubjectCode(String subjectCode);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM PrimarySubject s " +
            "WHERE s.subjectCode = :subjectCode " +
            "AND s.descriptiveTitle = :descriptiveTitle")
    boolean existsBySubjectCodeAndDescriptiveTitle(
            @Param("subjectCode") String subjectCode,
            @Param("descriptiveTitle") String descriptiveTitle
    );

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );

    @Query("""
    SELECT ps.legacyId
    FROM PrimarySubject ps
    WHERE ps.legacyDatabase = :database
""")
    List<String> findLegacyIdsByDatabase(String database);
}

