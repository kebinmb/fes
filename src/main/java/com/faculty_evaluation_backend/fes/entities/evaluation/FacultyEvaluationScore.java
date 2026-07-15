package com.faculty_evaluation_backend.fes.entities.evaluation;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.entities.primary.enums.EvaluationType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "faculty_evaluation_score",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_evaluation",
                columnNames = {
                        "faculty_id",
                        "evaluator_id",
                        "class_code",
                        "subject_code",
                        "year_level",
                        "semester",
                        "school_year"
                }
        )
)
public class FacultyEvaluationScore extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faculty_evaluation_score_id")
    private Long facultyEvaluationScoreId;

    @Column(name = "faculty_id", nullable = false, length = 60)
    private String facultyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "faculty_id",
            referencedColumnName = "faculty_id",
            insertable = false,
            updatable = false
    )
    private PrimaryFaculty faculty;

    @Column(name = "evaluator_id", nullable = false, length = 50)
    private String evaluatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "evaluator_id",
            referencedColumnName = "student_id",
            insertable = false,
            updatable = false
    )
    private PrimaryStudent student;

    @Column(name = "class_code", nullable = false, length = 255)
    private String classCode;

    @Column(name = "semester", nullable = false, length = 10)
    private String semester;

    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @Column(name = "subject_code", nullable = false)
    private String subjectCode;

    @Column(name = "year_level", nullable = false, length = 50)
    private String yearLevel;

    @Column(name = "comments_or_feedbacks")
    private String commentsOrFeedbacks;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "management_of_teaching_and_learning_id",
            referencedColumnName = "management_of_teaching_and_learning_id"
    )
    private ManagementOfTeachingAndLearning managementOfTeachingAndLearning;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "content_knowledge_pedagogy_and_technology_id",
            referencedColumnName = "content_knowledge_pedagogy_and_technology_id"
    )
    private ContentKnowledgePedagogyAndTechnology
            contentKnowledgePedagogyAndTechnology;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "commitment_and_transparency_id",
            referencedColumnName = "commitment_and_transparency_id"
    )
    private CommitmentAndTransparency commitmentAndTransparency;

    @Column(name = "overall_average_score")
    private Double overallAverageScore;

    @Column(name = "overall_interpretation", length = 20)
    private String overallInterpretation;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type")
    private EvaluationType evaluationType;
    /* =========================================================
       OVERALL COMPUTATION
       ========================================================= */

    public void calculateOverallScore() {

        if (managementOfTeachingAndLearning == null
                || contentKnowledgePedagogyAndTechnology == null
                || commitmentAndTransparency == null) {

            this.overallAverageScore = 0.0;
            this.overallInterpretation = "Incomplete";

            return;
        }

        double teachingScore =
                managementOfTeachingAndLearning.calculateAverageScore();

        double contentScore =
                contentKnowledgePedagogyAndTechnology.calculateAverageScore();

        double commitmentScore =
                commitmentAndTransparency.calculateAverageScore();

        /* =====================================================
           TOTAL RAW SCORE
           MAXIMUM POSSIBLE = 75
           ===================================================== */

        double totalScore =
                teachingScore
                        + contentScore
                        + commitmentScore;

        /* =====================================================
           SET RATING FORMULA

           ((TOTAL SCORE) / 75) * 100
           ===================================================== */

        double computedSetRating =
                (totalScore / 75.0) * 100.0;

        this.overallAverageScore =
                round(computedSetRating);

        this.overallInterpretation =
                determineOverallInterpretation(
                        computedSetRating
                );
    }

    /* =========================================================
       INTERPRETATION
       ========================================================= */

    private String determineOverallInterpretation(
            double rating
    ) {

        if (rating >= 90) {
            return "Excellent";
        }

        if (rating >= 80) {
            return "Very Good";
        }

        if (rating >= 70) {
            return "Good";
        }

        if (rating >= 60) {
            return "Fair";
        }

        return "Poor";
    }

    /* =========================================================
       ROUNDING
       ========================================================= */

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }

    /* =========================================================
       VALIDATION
       ========================================================= */

    public boolean isComplete() {

        return managementOfTeachingAndLearning != null
                && managementOfTeachingAndLearning.isComplete()

                && contentKnowledgePedagogyAndTechnology != null
                && contentKnowledgePedagogyAndTechnology.isComplete()

                && commitmentAndTransparency != null
                && commitmentAndTransparency.isComplete();
    }

    public boolean isAllPositive() {

        return managementOfTeachingAndLearning != null
                && managementOfTeachingAndLearning.isAllPositive()

                && contentKnowledgePedagogyAndTechnology != null
                && contentKnowledgePedagogyAndTechnology.isAllPositive()

                && commitmentAndTransparency != null
                && commitmentAndTransparency.isAllPositive();
    }

    /* =========================================================
       TOTAL CRITERIA
       ========================================================= */

    public static int getTotalCriteria() {

        return ManagementOfTeachingAndLearning.getTotalCriteria()

                + ContentKnowledgePedagogyAndTechnology.getTotalCriteria()

                + CommitmentAndTransparency.getTotalCriteria();
    }

    /* =========================================================
       SCORE BREAKDOWN
       ========================================================= */

    public String getScoreBreakdown() {

        if (!isComplete()) {
            return "Evaluation incomplete";
        }

        double teaching =
                managementOfTeachingAndLearning.calculateAverageScore();

        double content =
                contentKnowledgePedagogyAndTechnology.calculateAverageScore();

        double commitment =
                commitmentAndTransparency.calculateAverageScore();

        double total =
                teaching + content + commitment;

        return String.format(
                "Teaching: %.2f | " +
                        "Content: %.2f | " +
                        "Commitment: %.2f | " +
                        "Total: %.2f/75 | " +
                        "SET Rating: %.2f%% (%s)",

                teaching,
                content,
                commitment,
                total,
                overallAverageScore,
                overallInterpretation
        );
    }

    /* =========================================================
       PASSING CHECK
       ========================================================= */

    public boolean hasPassed() {

        return overallAverageScore != null
                && overallAverageScore >= 75.0;
    }

    /* =========================================================
       ENTITY LIFECYCLE
       ========================================================= */

    @PrePersist
    @PreUpdate
    public void prePersist() {

        calculateOverallScore();
    }
}
