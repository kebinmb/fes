package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyLoadDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrimaryClassRepository extends JpaRepository<PrimaryClass, Long> {
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM PrimaryClass c " + "WHERE c.classCode = :classCode " + "AND c.facultyId = :facultyId " + "AND c.subjectCode = :subjectCode " + "AND c.sectionId = :sectionId " + "AND c.schoolYear = :schoolYear " + "AND c.semester = :semester")
    boolean existsByClassCodeAndFacultyIdAndSubjectCodeAndSectionIdAndSchoolYearAndSemester(@Param("classCode") Integer classCode, @Param("facultyId") String facultyId, @Param("subjectCode") String subjectCode, @Param("sectionId") Integer sectionId, @Param("schoolYear") Integer schoolYear, @Param("semester") String semester);

    // Using native query to handle String/Integer type mismatch in JOIN
    @Query(value = "SELECT " + "pc.class_code AS classCode, " + "pc.faculty_id AS facultyId, " + "pc.subject_code AS subjectCode, " + "pc.section_id AS section, " + "pc.schedule_day AS scheduleDay, " + "pc.schedule_time AS scheduleTime, " + "pc.room AS room, " + "pc.semester AS semester, " + "pc.school_year AS schoolYear, " + "sl.primary_student_load_id AS studentLoadId, " + "sl.student_id AS studentId, " + "sl.grade AS grade " + "FROM primary_class pc " + "INNER JOIN primary_student_load sl ON pc.class_code = sl.class_code " + "WHERE sl.student_id = :studentId", nativeQuery = true)
    List<Object[]> findClassesByStudentIdNative(@Param("studentId") String studentId);

//    default List<StudentClassLoadDTO> findClassesByStudentId(String studentId) {
//        return findClassesByStudentIdNative(studentId).stream()
//                .map(row -> new StudentClassLoadDTO(
//                        (String) row[0],  // classCode
//                        (String) row[1],  // facultyId
//                        (String) row[2],  // subjectCode
//                        (Integer) row[3], // section
//                        (String) row[4],  // scheduleDay
//                        (String) row[5],  // scheduleTime
//                        (String) row[6],  // room
//                        (String) row[7],  // semester
//                        (Integer) row[8], // schoolYear
//                        ((Number) row[9]).longValue(), // studentLoadId
//                        (String) row[10], // studentId
//                        (String) row[11]  // grade
//                ))
//                .toList();
//    }

    @Query("SELECT pc FROM PrimaryClass pc " + "LEFT JOIN FETCH pc.faculty f " + "LEFT JOIN FETCH pc.section s " + "LEFT JOIN FETCH pc.subject sub " + "WHERE pc.classCode = :classCode")
    PrimaryClass findClassWithFacultyByClassCode(@Param("classCode") String classCode);

    @Query(value = "SELECT " + "pc.class_code, pc.faculty_id, pc.subject_code, pc.section_id, " + "pc.semester, pc.school_year, pc.schedule_day, pc.schedule_time, pc.room, " + "pf.firstname AS faculty_firstname, pf.lastname AS faculty_lastname, pf.position AS faculty_position, " + "ps.program_code AS section_program_code, ps.section_code, ps.year_level, " + "psub.descriptive_title, " + "pst.student_id, pst.student_firstname, pst.student_lastname, pst.program_code AS student_program_code, " + "psl.grade " + "FROM primary_class pc " + "LEFT JOIN primary_faculty pf ON pc.faculty_id = pf.faculty_id " + "LEFT JOIN primary_section ps ON pc.section_id = ps.section_id " + "LEFT JOIN primary_subject psub ON pc.subject_code = psub.subject_code " + "INNER JOIN primary_student_load psl ON CAST(pc.class_code AS UNSIGNED) = psl.class_code " + "INNER JOIN primary_student pst ON psl.student_id = pst.student_id " + "WHERE pc.class_code = :classCode " + "AND psl.student_id = :studentId", nativeQuery = true)
    Object[] findClassWithDetailsForStudentNative(@Param("classCode") String classCode, @Param("studentId") String studentId);

    @Query(value = """
    SELECT
        pc.subject_code AS subjectCode,
        pc.faculty_id AS facultyId,
        pc.school_year AS schoolYear,
        pc.semester AS semester,

        MIN(pc.class_code) AS classCode,

        MIN(psl.year_level) AS yearLevel,

        ps.program_code AS programCode,

        MIN(ps.section_code) AS sectionCode

    FROM primary_class pc

    INNER JOIN primary_student_load psl
        ON pc.class_code = psl.class_code

    INNER JOIN primary_section ps
        ON pc.section_id = ps.section_id

    WHERE pc.faculty_id = :facultyId
      AND ps.program_code = :program
      AND pc.school_year = :schoolYear
      AND pc.semester = :semester

    GROUP BY
        pc.subject_code,
        pc.faculty_id,
        pc.school_year,
        pc.semester,
        ps.program_code

    ORDER BY
        pc.subject_code,
        ps.program_code
    """, nativeQuery = true)
    List<FacultyClassDTO> findFacultyClasses(
            @Param("facultyId") String facultyId,
            @Param("program") String program,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );
    boolean existsByLegacyDatabaseAndLegacyId(String legacyDatabase, String legacyId);

    @Query(value = """
    SELECT DISTINCT

        pf.faculty_id AS facultyId,

        pf.firstname AS firstname,
        pf.lastname AS lastname,
        pf.middlename AS middlename,

        pf.position AS position,

        pc.source_campus AS campus,

        pf.load_limit AS loadLimit,

        CASE
            WHEN COUNT(*) OVER (PARTITION BY pc.faculty_id) > pf.load_limit
                THEN 'OVERLOAD'
            ELSE 'REGULAR'
        END AS typeOfLoad

    FROM primary_class pc

    INNER JOIN primary_faculty pf
        ON pc.faculty_id = pf.faculty_id

    INNER JOIN primary_section ps
        ON pc.section_id = ps.section_id

    INNER JOIN user_accounts ua
        ON ua.data_source = pf.legacy_database

    WHERE pc.class_code IS NOT NULL
        AND ps.program_code = :programCode
        AND ps.section_code LIKE CONCAT('%', :sectionCode, '%')
        AND ua.user_id = :userId

    ORDER BY
        pf.lastname,
        pf.firstname
    """, nativeQuery = true)
    List<FacultyLoadDTO> findFacultyLoadsByProgram(
            @Param("programCode") String programCode,
            @Param("sectionCode") String sectionCode,
            @Param("userId") Long userId
    );
}
