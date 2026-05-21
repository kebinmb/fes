package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyLoadDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

            GROUP_CONCAT(
                DISTINCT pc.class_code
                ORDER BY pc.class_code
            ) AS classCodes,

            GROUP_CONCAT(
                DISTINCT psl.yearLevels
                ORDER BY psl.yearLevels
            ) AS yearLevels,

            GROUP_CONCAT(
                DISTINCT ps.program_code
                ORDER BY ps.program_code
            ) AS programCodes,

            GROUP_CONCAT(
                DISTINCT ps.section_code
                ORDER BY ps.section_code
            ) AS sectionCodes

        FROM primary_class pc

        INNER JOIN primary_section ps
            ON pc.section_id = ps.section_id

        LEFT JOIN (
            SELECT
                class_code,
                GROUP_CONCAT(
                    DISTINCT year_level
                    ORDER BY year_level
                ) AS yearLevels
            FROM primary_student_load
            GROUP BY class_code
        ) psl
            ON pc.class_code = psl.class_code

        WHERE pc.faculty_id = :facultyId
          AND pc.school_year = :schoolYear
          AND pc.semester = :semester

        GROUP BY
            pc.subject_code,
            pc.faculty_id,
            pc.school_year,
            pc.semester

        ORDER BY
            pc.subject_code
        """,
            nativeQuery = true)
    List<FacultyClassDTO> findFacultyClasses(

            @Param("facultyId") String facultyId,

            @Param("schoolYear") Integer schoolYear,

            @Param("semester") String semester
    );
    boolean existsByLegacyDatabaseAndLegacyId(String legacyDatabase, String legacyId);

    @Query(value = """

    SELECT

        pf.faculty_id AS facultyId,
        pf.firstname AS firstname,
        pf.lastname AS lastname,
        pf.middlename AS middlename,
        pf.position AS position,
        MAX(pc.source_campus) AS campus,
        pf.load_limit AS loadLimit,
        pf.college AS college,

        CASE
            WHEN COALESCE(fl.total_load, 0) > pf.load_limit
                THEN 'OVERLOAD'
            ELSE 'REGULAR'
        END AS typeOfLoad

    FROM primary_class pc

    INNER JOIN primary_faculty pf
        ON pc.faculty_id = pf.faculty_id

    /* LOGGED-IN USER */
    INNER JOIN user_accounts ua
        ON ua.user_id = :userId

    LEFT JOIN (

        SELECT
            pc2.faculty_id,
            COUNT(DISTINCT pc2.subject_code) AS total_load

        FROM primary_class pc2

        GROUP BY pc2.faculty_id

    ) fl
        ON fl.faculty_id = pf.faculty_id

    WHERE pc.class_code IS NOT NULL

      AND LOWER(pf.status) = 'active'

      /* SAME DATASOURCE ONLY */
      AND LOWER(ua.data_source) =
          LOWER(pf.legacy_database)

      /* EXCLUDE LOGGED-IN USER */
      AND LOWER(CONCAT(
            COALESCE(ua.firstname, ''),
            ' ',
            COALESCE(ua.lastname, '')
      )) != LOWER(CONCAT(
            COALESCE(pf.firstname, ''),
            ' ',
            COALESCE(pf.lastname, '')
      ))

      /* EXCLUDE PROGRAM CHAIR */
      AND LOWER(pf.position) != 'program_chair'

      AND (
            :search IS NULL
            OR :search = ''
            OR LOWER(pf.firstname)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(pf.lastname)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(pf.middlename)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(CONCAT(
                    pf.firstname,
                    ' ',
                    pf.lastname
               ))
               LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(CONCAT(
                    pf.lastname,
                    ', ',
                    pf.firstname
               ))
               LIKE LOWER(CONCAT('%', :search, '%'))
      )

    GROUP BY
        pf.faculty_id,
        pf.firstname,
        pf.lastname,
        pf.middlename,
        pf.position,
        pf.load_limit,
        pf.college,
        fl.total_load

    ORDER BY
        pf.lastname ASC,
        pf.firstname ASC
    """,

            countQuery = """

    SELECT COUNT(DISTINCT pf.faculty_id)

    FROM primary_class pc

    INNER JOIN primary_faculty pf
        ON pc.faculty_id = pf.faculty_id

    INNER JOIN user_accounts ua
        ON ua.user_id = :userId

    WHERE pc.class_code IS NOT NULL

      AND LOWER(pf.status) = 'active'

      /* SAME DATASOURCE ONLY */
      AND LOWER(ua.data_source) =
          LOWER(pf.legacy_database)

      /* EXCLUDE LOGGED-IN USER */
      AND LOWER(CONCAT(
            COALESCE(ua.firstname, ''),
            ' ',
            COALESCE(ua.lastname, '')
      )) != LOWER(CONCAT(
            COALESCE(pf.firstname, ''),
            ' ',
            COALESCE(pf.lastname, '')
      ))

      /* EXCLUDE PROGRAM CHAIR */
      AND LOWER(pf.position) != 'program_chair'

      AND (
            :search IS NULL
            OR :search = ''
            OR LOWER(pf.firstname)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(pf.lastname)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(pf.middlename)
                LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(CONCAT(
                    pf.firstname,
                    ' ',
                    pf.lastname
               ))
               LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(CONCAT(
                    pf.lastname,
                    ', ',
                    pf.firstname
               ))
               LIKE LOWER(CONCAT('%', :search, '%'))
      )
    """,

            nativeQuery = true)
    Page<FacultyLoadDTO> findFacultyLoadsByProgram(

            @Param("userId") Long userId,

            @Param("search") String search,

            Pageable pageable
    );
}
