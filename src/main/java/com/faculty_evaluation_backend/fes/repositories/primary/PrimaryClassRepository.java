package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyLoadDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyProgramLoadsDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PrimaryClassRepository extends JpaRepository<PrimaryClass, Long> {
    @Query(
            value = """
                    SELECT pc
                    FROM PrimaryClass pc
                    LEFT JOIN FETCH pc.faculty faculty
                    LEFT JOIN FETCH pc.section section
                    LEFT JOIN FETCH pc.subject subject
                    WHERE pc.schoolYear = :schoolYear
                      AND LOWER(TRIM(pc.semester)) = LOWER(TRIM(:semester))
                      AND (
                            :legacyDatabase IS NULL
                            OR :legacyDatabase = ''
                            OR UPPER(TRIM(pc.legacyDatabase)) = UPPER(TRIM(:legacyDatabase))
                      )
                      AND (
                            :search IS NULL
                            OR :search = ''
                            OR LOWER(COALESCE(pc.classCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(pc.subjectCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(subject.descriptiveTitle, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(pc.facultyId, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(CONCAT(COALESCE(faculty.firstname, ''), ' ', COALESCE(faculty.lastname, '')))
                               LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(section.programCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(section.sectionCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                      )
                    """,
            countQuery = """
                    SELECT COUNT(pc)
                    FROM PrimaryClass pc
                    LEFT JOIN pc.faculty faculty
                    LEFT JOIN pc.section section
                    LEFT JOIN pc.subject subject
                    WHERE pc.schoolYear = :schoolYear
                      AND LOWER(TRIM(pc.semester)) = LOWER(TRIM(:semester))
                      AND (
                            :legacyDatabase IS NULL
                            OR :legacyDatabase = ''
                            OR UPPER(TRIM(pc.legacyDatabase)) = UPPER(TRIM(:legacyDatabase))
                      )
                      AND (
                            :search IS NULL
                            OR :search = ''
                            OR LOWER(COALESCE(pc.classCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(pc.subjectCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(subject.descriptiveTitle, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(pc.facultyId, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(CONCAT(COALESCE(faculty.firstname, ''), ' ', COALESCE(faculty.lastname, '')))
                               LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(section.programCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(section.sectionCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                      )
                    """
    )
    Page<PrimaryClass> findAdminClassAssignments(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            @Param("search") String search,
            @Param("legacyDatabase") String legacyDatabase,
            Pageable pageable
    );

    @Query("""
            SELECT pc
            FROM PrimaryClass pc
            LEFT JOIN FETCH pc.faculty
            LEFT JOIN FETCH pc.section
            LEFT JOIN FETCH pc.subject
            WHERE pc.primaryClassId = :primaryClassId
            """)
    Optional<PrimaryClass> findAssignmentById(
            @Param("primaryClassId") Long primaryClassId
    );

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM PrimaryClass c " + "WHERE c.classCode = :classCode " + "AND c.facultyId = :facultyId " + "AND c.subjectCode = :subjectCode " + "AND c.sectionId = :sectionId " + "AND c.schoolYear = :schoolYear " + "AND c.semester = :semester")
    boolean existsByClassCodeAndFacultyIdAndSubjectCodeAndSectionIdAndSchoolYearAndSemester(@Param("classCode") String classCode, @Param("facultyId") String facultyId, @Param("subjectCode") String subjectCode, @Param("sectionId") Integer sectionId, @Param("schoolYear") Integer schoolYear, @Param("semester") String semester);

    // Using native query to handle String/Integer type mismatch in JOIN
    @Query(value = "SELECT " + "pc.class_code AS classCode, " + "pc.faculty_id AS facultyId, " + "pc.subject_code AS subjectCode, " + "pc.section_id AS section, " + "pc.schedule_day AS scheduleDay, " + "pc.schedule_time AS scheduleTime, " + "pc.room AS room, " + "pc.semester AS semester, " + "pc.school_year AS schoolYear, " + "sl.primary_student_load_id AS studentLoadId, " + "sl.student_id AS studentId, " + "sl.grade AS grade " + "FROM primary_class pc " + "INNER JOIN primary_student_load sl ON (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(sl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) " + "WHERE (CAST(sl.student_id AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(:studentId AS CHAR) COLLATE utf8mb4_unicode_ci)", nativeQuery = true)
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

    @Query(value = "SELECT " + "pc.class_code, pc.faculty_id, pc.subject_code, pc.section_id, " + "pc.semester, pc.school_year, pc.schedule_day, pc.schedule_time, pc.room, " + "pf.firstname AS faculty_firstname, pf.lastname AS faculty_lastname, pf.position AS faculty_position, " + "ps.program_code AS section_program_code, ps.section_code, ps.year_level, " + "psub.descriptive_title, " + "pst.student_id, pst.student_firstname, pst.student_lastname, pst.program_code AS student_program_code, " + "psl.grade " + "FROM primary_class pc " + "LEFT JOIN primary_faculty pf ON (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) " + "LEFT JOIN primary_section ps ON pc.section_id = ps.section_id " + "LEFT JOIN primary_subject psub ON (CAST(pc.subject_code AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(psub.subject_code AS CHAR) COLLATE utf8mb4_unicode_ci) " + "INNER JOIN primary_student_load psl ON (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) " + "INNER JOIN primary_student pst ON (CAST(psl.student_id AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(pst.student_id AS CHAR) COLLATE utf8mb4_unicode_ci) " + "WHERE (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(:classCode AS CHAR) COLLATE utf8mb4_unicode_ci) " + "AND (CAST(psl.student_id AS CHAR) COLLATE utf8mb4_unicode_ci) = (CAST(:studentId AS CHAR) COLLATE utf8mb4_unicode_ci)", nativeQuery = true)
    Object[] findClassWithDetailsForStudentNative(@Param("classCode") String classCode, @Param("studentId") String studentId);

    @Query(value = """
            SELECT
                pc.subject_code AS subjectCode,
                pc.faculty_id AS facultyId,
                pc.school_year AS schoolYear,
                pc.semester AS semester,
                MIN(pc.class_code) AS classCode,
                GROUP_CONCAT(DISTINCT ps.year_level ORDER BY ps.year_level SEPARATOR ', ') AS yearLevel,
                GROUP_CONCAT(DISTINCT ps.program_code ORDER BY ps.program_code SEPARATOR ', ') AS programCode,
                GROUP_CONCAT(DISTINCT ps.section_code ORDER BY ps.section_code SEPARATOR ', ') AS sectionCode
            FROM primary_class pc
            INNER JOIN primary_section ps
                ON pc.section_id = ps.section_id
            WHERE pc.faculty_id = :facultyId
              AND pc.school_year = :schoolYear
              AND pc.semester = :semester
              AND (
                    :programCode IS NULL
                    OR :programCode = ''
                    OR (UPPER(TRIM(CAST(ps.program_code AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                       (UPPER(TRIM(CAST(:programCode AS CHAR))) COLLATE utf8mb4_unicode_ci)
              )
              AND pc.class_code IS NOT NULL
              AND pc.subject_code IS NOT NULL
              AND pc.section_id IS NOT NULL
            GROUP BY
                pc.subject_code,
                pc.faculty_id,
                pc.school_year,
                pc.semester
            ORDER BY
                pc.subject_code ASC,
                sectionCode ASC,
                classCode ASC
            """, nativeQuery = true)
    List<FacultyClassDTO> findFacultyClasses(

            @Param("facultyId") String facultyId,

            @Param("schoolYear") Integer schoolYear,

            @Param("semester") String semester,

            @Param("programCode") String programCode);

    @Query(value = """
            SELECT COUNT(*)
            FROM primary_class pc
            INNER JOIN primary_faculty pf
                ON (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            INNER JOIN user_accounts ua
                ON ua.user_id = :userId
               AND (UPPER(TRIM(CAST(ua.data_source AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                   (UPPER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci)
            WHERE (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                  (CAST(:facultyId AS CHAR) COLLATE utf8mb4_unicode_ci)
              AND pc.school_year = :schoolYear
              AND (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                  (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
            """, nativeQuery = true)
    long countFacultyInSupervisorScope(
            @Param("userId") Long userId,
            @Param("facultyId") String facultyId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT
                pc.class_code AS classCode,
                pc.subject_code AS courseCode,
                ps.section_id AS sectionId,
                ps.program_code AS programCode,
                ps.year_level AS yearLevel,
                ps.section_code AS sectionCode
            FROM primary_class pc
            INNER JOIN primary_section ps
                ON ps.section_id = pc.section_id
            LEFT JOIN faculty_workload fw
                ON (CAST(fw.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
               AND fw.school_year = pc.school_year
               AND (
                    (LOWER(TRIM(CAST(fw.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                    (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    OR (
                        UPPER(TRIM(CAST(fw.semester AS CHAR))) = 'FIRST_SEMESTER'
                        AND LOWER(TRIM(CAST(pc.semester AS CHAR))) = '1st'
                    )
                    OR (
                        UPPER(TRIM(CAST(fw.semester AS CHAR))) = 'SECOND_SEMESTER'
                        AND LOWER(TRIM(CAST(pc.semester AS CHAR))) = '2nd'
                    )
                    OR (
                        UPPER(TRIM(CAST(fw.semester AS CHAR))) = 'SUMMER_SEMESTER'
                        AND LOWER(TRIM(CAST(pc.semester AS CHAR))) = 'summer'
                    )
               )
               AND (
                    (CAST(fw.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                    (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                    OR (
                        fw.class_code IS NULL
                        AND (CAST(fw.course_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                            (CAST(pc.subject_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                        AND (CAST(fw.program_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                            (CAST(ps.program_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                        AND (CAST(fw.year_level AS CHAR) COLLATE utf8mb4_unicode_ci) =
                            (CAST(ps.year_level AS CHAR) COLLATE utf8mb4_unicode_ci)
                        AND (CAST(fw.section_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                            (CAST(ps.section_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                    )
               )
            WHERE (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                  (CAST(:facultyId AS CHAR) COLLATE utf8mb4_unicode_ci)
              AND pc.school_year = :schoolYear
              AND (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                  (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
              AND pc.subject_code IS NOT NULL
              AND pc.section_id IS NOT NULL
              AND pc.class_code IS NOT NULL
              AND fw.faculty_workload_id IS NULL
            ORDER BY
                pc.subject_code ASC,
                ps.program_code ASC,
                ps.year_level ASC,
                ps.section_code ASC,
                pc.class_code ASC
            """, nativeQuery = true)
    List<Object[]> findFacultyWorkloadClassOptionRows(
            @Param("facultyId") String facultyId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    boolean existsByLegacyDatabaseAndLegacyId(String legacyDatabase, String legacyId);

    @Query("""
                SELECT pc.legacyId
                FROM PrimaryClass pc
                WHERE pc.legacyDatabase = :database
            """)
    Set<String> findLegacyIdsByDatabase(@Param("database") String database);

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
                ON (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            
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
                ON (CAST(fl.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            
            WHERE pc.class_code IS NOT NULL
            
              AND LOWER(pf.status) = 'active'
            
              /* SAME DATASOURCE ONLY */
              AND (LOWER(TRIM(CAST(ua.data_source AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                  (LOWER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci)
            
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
                        ON (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    
                    INNER JOIN user_accounts ua
                        ON ua.user_id = :userId
                    
                    WHERE pc.class_code IS NOT NULL
                    
                      AND LOWER(pf.status) = 'active'
                    
                      /* SAME DATASOURCE ONLY */
                      AND (LOWER(TRIM(CAST(ua.data_source AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                          (LOWER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    
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

            Pageable pageable);

    @Query(value = """

        SELECT DISTINCT
            pf.faculty_id AS facultyId,
            pf.firstname AS firstname,
            pf.lastname AS lastname,
            pf.status AS status,
            pf.position AS position,
            pf.college AS college,
            pc.source_campus AS campus

        FROM primary_faculty pf

        INNER JOIN primary_class pc
            ON (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
               (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)

        INNER JOIN primary_section ps
            ON pc.section_id = ps.section_id

        INNER JOIN user_accounts ua
            ON ua.user_id = :userId
           AND (UPPER(TRIM(CAST(ua.data_source AS CHAR))) COLLATE utf8mb4_unicode_ci)
               = (UPPER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci)

        WHERE (

                :search IS NULL
                OR :search = ''
                OR LOWER(pf.firstname)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(pf.lastname)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CONCAT(
                        pf.firstname,
                        ' ',
                        pf.lastname
                ))
                    LIKE LOWER(CONCAT('%', :search, '%'))

        )

        AND (

                :campus IS NULL
                OR :campus = ''
                OR (UPPER(TRIM(CAST(pc.source_campus AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    = (UPPER(TRIM(CAST(:campus AS CHAR))) COLLATE utf8mb4_unicode_ci)

        )

        AND (

                :programCode IS NULL
                OR :programCode = ''
                OR (UPPER(TRIM(CAST(ps.program_code AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    = (UPPER(TRIM(CAST(:programCode AS CHAR))) COLLATE utf8mb4_unicode_ci)

        )

        AND UPPER(pf.status) = 'ACTIVE'

        ORDER BY
            pf.lastname ASC,
            pf.firstname ASC

        """,

            countQuery = """

        SELECT COUNT(DISTINCT pf.faculty_id)

        FROM primary_faculty pf

        INNER JOIN primary_class pc
            ON (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
               (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)

        INNER JOIN primary_section ps
            ON pc.section_id = ps.section_id

        INNER JOIN user_accounts ua
            ON ua.user_id = :userId
           AND (UPPER(TRIM(CAST(ua.data_source AS CHAR))) COLLATE utf8mb4_unicode_ci)
               = (UPPER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci)

        WHERE (

                :search IS NULL
                OR :search = ''
                OR LOWER(pf.firstname)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(pf.lastname)
                    LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(CONCAT(
                        pf.firstname,
                        ' ',
                        pf.lastname
                ))
                    LIKE LOWER(CONCAT('%', :search, '%'))

        )

        AND (

                :campus IS NULL
                OR :campus = ''
                OR (UPPER(TRIM(CAST(pc.source_campus AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    = (UPPER(TRIM(CAST(:campus AS CHAR))) COLLATE utf8mb4_unicode_ci)

        )

        AND (

                :programCode IS NULL
                OR :programCode = ''
                OR (UPPER(TRIM(CAST(ps.program_code AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    = (UPPER(TRIM(CAST(:programCode AS CHAR))) COLLATE utf8mb4_unicode_ci)

        )

        AND UPPER(pf.status) = 'ACTIVE'

        """,

            nativeQuery = true)
    Page<FacultyProgramLoadsDTO> findFacultyPerProgram(

            @Param("userId") Long userId,

            @Param("search") String search,

            @Param("campus") String campus,

            @Param("programCode") String programCode,

            Pageable pageable
    );
}
