package com.faculty_evaluation_backend.fes.repositories.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationEvidence;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.EvaluationEvidenceCriterion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FacultyEvaluationEvidenceRepositoryImpl
        implements FacultyEvaluationEvidenceRepositoryCustom {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "originalFilename",
            "criterion",
            "schoolYear",
            "semester",
            "classCode",
            "subjectCode",
            "uploadedBy",
            "fileSize"
    );

    @PersistenceContext(unitName = "primary")
    private EntityManager entityManager;

    @Override
    public Page<FacultyEvaluationEvidence> findByFilters(
            String facultyId,
            String classCode,
            String subjectCode,
            String semester,
            Integer schoolYear,
            EvaluationEvidenceCriterion criterion,
            Pageable pageable
    ) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FacultyEvaluationEvidence> query =
                builder.createQuery(FacultyEvaluationEvidence.class);
        Root<FacultyEvaluationEvidence> root =
                query.from(FacultyEvaluationEvidence.class);

        query.select(root)
                .where(filters(
                        builder,
                        root,
                        facultyId,
                        classCode,
                        subjectCode,
                        semester,
                        schoolYear,
                        criterion
                ))
                .orderBy(sortOrders(builder, root, pageable.getSort()));

        List<FacultyEvaluationEvidence> content =
                pagedQuery(query, pageable, pageable.getPageSize())
                        .getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<FacultyEvaluationEvidence> countRoot =
                countQuery.from(FacultyEvaluationEvidence.class);

        countQuery.select(builder.count(countRoot))
                .where(filters(
                        builder,
                        countRoot,
                        facultyId,
                        classCode,
                        subjectCode,
                        semester,
                        schoolYear,
                        criterion
                ));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Slice<FacultyEvaluationEvidence> findSliceByFilters(
            String facultyId,
            String classCode,
            String subjectCode,
            String semester,
            Integer schoolYear,
            EvaluationEvidenceCriterion criterion,
            Pageable pageable
    ) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FacultyEvaluationEvidence> query =
                builder.createQuery(FacultyEvaluationEvidence.class);
        Root<FacultyEvaluationEvidence> root =
                query.from(FacultyEvaluationEvidence.class);

        query.select(root)
                .where(filters(
                        builder,
                        root,
                        facultyId,
                        classCode,
                        subjectCode,
                        semester,
                        schoolYear,
                        criterion
                ))
                .orderBy(sortOrders(builder, root, pageable.getSort()));

        List<FacultyEvaluationEvidence> results =
                pagedQuery(query, pageable, pageable.getPageSize() + 1)
                        .getResultList();
        boolean hasNext = results.size() > pageable.getPageSize();
        List<FacultyEvaluationEvidence> content = hasNext
                ? results.subList(0, pageable.getPageSize())
                : results;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private Predicate[] filters(
            CriteriaBuilder builder,
            Root<FacultyEvaluationEvidence> root,
            String facultyId,
            String classCode,
            String subjectCode,
            String semester,
            Integer schoolYear,
            EvaluationEvidenceCriterion criterion
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.equal(root.get("facultyId"), facultyId));

        if (classCode != null) {
            predicates.add(builder.equal(root.get("classCode"), classCode));
        }

        if (subjectCode != null) {
            predicates.add(builder.equal(root.get("subjectCode"), subjectCode));
        }

        if (semester != null) {
            predicates.add(builder.equal(root.get("semester"), semester));
        }

        if (schoolYear != null) {
            predicates.add(builder.equal(root.get("schoolYear"), schoolYear));
        }

        if (criterion != null) {
            predicates.add(builder.equal(root.get("criterion"), criterion));
        }

        return predicates.toArray(Predicate[]::new);
    }

    private List<Order> sortOrders(
            CriteriaBuilder builder,
            Root<FacultyEvaluationEvidence> root,
            Sort sort
    ) {
        List<Order> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                continue;
            }

            orders.add(order.isAscending()
                    ? builder.asc(root.get(order.getProperty()))
                    : builder.desc(root.get(order.getProperty())));
        }

        if (orders.isEmpty()) {
            orders.add(builder.desc(root.get("createdAt")));
        }

        orders.add(builder.desc(root.get("facultyEvaluationEvidenceId")));

        return orders;
    }

    private TypedQuery<FacultyEvaluationEvidence> pagedQuery(
            CriteriaQuery<FacultyEvaluationEvidence> query,
            Pageable pageable,
            int maxResults
    ) {
        return entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(maxResults);
    }
}
