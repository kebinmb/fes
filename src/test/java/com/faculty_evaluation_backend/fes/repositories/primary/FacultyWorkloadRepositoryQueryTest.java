package com.faculty_evaluation_backend.fes.repositories.primary;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FacultyWorkloadRepositoryQueryTest {

    @Test
    void workloadSearchAcceptsEquivalentSemesterFormats() throws Exception {
        Method method = FacultyWorkloadRepository.class.getMethod(
                "searchWorkloads",
                String.class,
                Integer.class,
                String.class,
                Pageable.class
        );

        Query query = method.getAnnotation(Query.class);

        assertEquivalentSemesterPredicate(normalize(query.value()));
        assertEquivalentSemesterPredicate(normalize(query.countQuery()));
    }

    @Test
    void classOptionQueryAcceptsEquivalentSemesterFormats() throws Exception {
        Method method = PrimaryClassRepository.class.getMethod(
                "findFacultyWorkloadClassOptionRows",
                String.class,
                Integer.class,
                String.class
        );

        Query query = method.getAnnotation(Query.class);

        assertEquivalentSemesterPredicate(normalize(query.value()));
    }

    private void assertEquivalentSemesterPredicate(String sql) {
        assertTrue(sql.contains("upper(trim(:semester)) in ('1st', 'first_semester')"));
        assertTrue(sql.contains("upper(trim(:semester)) in ('2nd', 'second_semester')"));
        assertTrue(sql.contains("upper(trim(:semester)) in ('summer', 'summer_semester')"));
    }

    private String normalize(String sql) {
        return sql
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();
    }
}
