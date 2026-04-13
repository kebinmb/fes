package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrimaryStudentLoadRepository extends JpaRepository<PrimaryStudentLoad, Long> {

    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM PrimaryStudentLoad sl " +
           "WHERE sl.loadId = :loadId " +
           "AND sl.studentId = :studentId " +
           "AND (sl.yearLevel = :yearLevel OR (sl.yearLevel IS NULL AND :yearLevel IS NULL)) " +
           "AND sl.classCode = :classCode")
    boolean existsByLoadIdAndStudentIdAndYearLevelAndClassCode(
            @Param("loadId") Integer loadId,
            @Param("studentId") String studentId,
            @Param("yearLevel") String yearLevel,
            @Param("classCode") Integer classCode
    );
    Integer countDistinctTotalPrimaryStudentLoadByStudentId(String studentId);

    @Query("""
    SELECT new com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO(
        pc.classCode,
        pc.facultyId,
        pc.subjectCode,
        pc.sectionId,
        pc.semester,
        pc.schoolYear,
        psl.studentId,
        psl.yearLevel,
        CONCAT(f.firstname, ' ', f.lastname),
        s.descriptiveTitle
    )
    FROM PrimaryStudentLoad psl
    JOIN psl.primaryClass pc
    JOIN pc.faculty f
    JOIN pc.subject s
    WHERE psl.studentId = :studentId
      AND psl.yearLevel = :yearLevel
      AND pc.schoolYear = :schoolYear
""")
    Page<StudentClassLoadDTO> findStudentLoadDTO(
            @Param("studentId") String studentId,
            @Param("yearLevel") String yearLevel,
            @Param("schoolYear") Integer schoolYear,
            Pageable pageable
    );
}
