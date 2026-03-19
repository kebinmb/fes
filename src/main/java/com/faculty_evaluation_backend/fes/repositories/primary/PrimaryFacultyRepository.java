package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrimaryFacultyRepository extends JpaRepository<PrimaryFaculty, Long> {
    Optional<PrimaryFaculty> findByFacultyId(String facultyId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM PrimaryFaculty f " +
            "WHERE f.facultyId = :facultyId " +
            "AND f.firstname = :firstname " +
            "AND f.lastname = :lastname " +
            "AND f.position = :position " +
            "AND f.loadLimit = :loadLimit " +
            "AND (f.middlename = :middlename OR (f.middlename IS NULL AND :middlename IS NULL))")
    boolean existsByFacultyIdAndFirstnameAndLastnameAndPositionAndLoadLimitAndMiddlename(
            @Param("facultyId") String facultyId,
            @Param("firstname") String firstname,
            @Param("lastname") String lastname,
            @Param("position") String position,
            @Param("loadLimit") Double loadLimit,
            @Param("middlename") String middlename
    );

List<PrimaryFaculty> findByCollegeAndStatus(College college, Status status);
}
