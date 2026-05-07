package com.faculty_evaluation_backend.fes.entities.evaluation;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "content_knowledge_pedagogy_and_technology")
public class ContentKnowledgePedagogyAndTechnology extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_knowledge_pedagogy_and_technology_id")
    private Long contentKnowledgePedagogyAndTechnologyId;

    @Column(name = "faculty_id", length = 60)
    private String facultyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_knowledge", nullable = false, length = 50)
    private RatingScale subjectKnowledge;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_simplification", nullable = false, length = 50)
    private RatingScale contentSimplification;

    @Enumerated(EnumType.STRING)
    @Column(name = "real_world_application", nullable = false, length = 50)
    private RatingScale realWorldApplication;

    @Enumerated(EnumType.STRING)
    @Column(name = "technology_integration", nullable = false, length = 50)
    private RatingScale technologyIntegration;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_alignment", nullable = false, length = 50)
    private RatingScale assessmentAlignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "faculty_id",
            referencedColumnName = "faculty_id",
            insertable = false,
            updatable = false
    )
    private PrimaryFaculty faculty;

    public double calculateAverageScore() {

        if (!isComplete()) {
            return 0.0;
        }

        return subjectKnowledge.getScore()
                + contentSimplification.getScore()
                + realWorldApplication.getScore()
                + technologyIntegration.getScore()
                + assessmentAlignment.getScore();
    }

    public double calculatePercentageScore() {

        // MAX = 5 criteria × 5 = 25
        return (calculateAverageScore() / 25.0) * 100.0;
    }

    public String getOverallInterpretation() {

        double percentage = calculatePercentageScore();

        if (percentage >= 90) return "Excellent";
        if (percentage >= 80) return "Very Good";
        if (percentage >= 70) return "Good";
        if (percentage >= 60) return "Fair";

        return "Poor";
    }

    public boolean isAllPositive() {

        return subjectKnowledge.isPositive()
                && contentSimplification.isPositive()
                && realWorldApplication.isPositive()
                && technologyIntegration.isPositive()
                && assessmentAlignment.isPositive();
    }

    public boolean isComplete() {

        return subjectKnowledge != null
                && contentSimplification != null
                && realWorldApplication != null
                && technologyIntegration != null
                && assessmentAlignment != null;
    }

    public boolean hasStrongTechnologyIntegration() {

        return technologyIntegration != null
                && technologyIntegration.isPositive();
    }

    public boolean hasExcellentSubjectKnowledge() {

        return subjectKnowledge != null
                && subjectKnowledge == RatingScale.ALWAYS_MANIFESTED;
    }

    public static int getTotalCriteria() {
        return 5;
    }
}