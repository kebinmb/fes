package com.faculty_evaluation_backend.fes.repositories.evaluation;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacultyEvaluationScoreRepositoryQueryTest {

    @Test
    void evaluatedStudentsQueryStaysScopedAndAvoidsLegacySlowJoin() throws Exception {
        Method method = FacultyEvaluationScoreRepository.class.getMethod(
                "findEvaluatedStudents",
                String.class,
                Integer.class,
                String.class,
                String.class,
                Pageable.class
        );

        Query query = method.getAnnotation(Query.class);
        String valueSql = normalize(query.value());
        String countSql = normalize(query.countQuery());

        assertEvaluatedStudentsQueryShape(valueSql);
        assertEvaluatedStudentsQueryShape(countSql);
        assertTrue(valueSql.contains("order by fes.created_at desc"));
    }

    private void assertEvaluatedStudentsQueryShape(String sql) {
        assertTrue(sql.contains("from primary_student ps"));
        assertTrue(sql.contains("inner join faculty_evaluation_score fes"));
        assertTrue(sql.contains("fes.school_year = :schoolyear"));
        assertTrue(sql.contains("fes.semester = :semester"));
        assertTrue(sql.contains("ps.legacy_database = :legacydatabase"));
        assertTrue(sql.contains("fes.evaluation_type = 'role_student'"));

        assertFalse(sql.contains("inner join user_accounts"));
        assertFalse(sql.contains("ua.data_source"));
        assertFalse(sql.contains("cast(fes.evaluator_id as char)"));
        assertFalse(sql.contains("cast(ps.student_id as char)"));
    }

    private String normalize(String sql) {
        return sql
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}
