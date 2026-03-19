package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
