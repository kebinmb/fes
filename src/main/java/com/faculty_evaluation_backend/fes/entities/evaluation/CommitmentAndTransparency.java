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
@Table(name = "commitment_and_transparency")
public class CommitmentAndTransparency extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commitment_and_transparency_id")
    private Long commitmentAndTransparencyId;

    @Column(name = "faculty_id", length = 60)
    private String facultyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "diversity_recognition", nullable = false, length = 50)
    private RatingScale diversityRecognition;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_support", nullable = false, length = 50)
    private RatingScale consultationSupport;

    @Enumerated(EnumType.STRING)
    @Column(name = "immediate_feedback", nullable = false, length = 50)
    private RatingScale immediateFeedback;

    @Enumerated(EnumType.STRING)
    @Column(name = "transparent_grading", nullable = false, length = 50)
    private RatingScale transparentGrading;

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

        return diversityRecognition.getScore()
                + consultationSupport.getScore()
                + immediateFeedback.getScore()
                + transparentGrading.getScore();
    }

    public double calculatePercentageScore() {

        // MAX = 4 criteria × 5 = 20
        return (calculateAverageScore() / 20.0) * 100.0;
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

        return diversityRecognition.isPositive()
                && consultationSupport.isPositive()
                && immediateFeedback.isPositive()
                && transparentGrading.isPositive();
    }

    public boolean isComplete() {

        return diversityRecognition != null
                && consultationSupport != null
                && immediateFeedback != null
                && transparentGrading != null;
    }

    public boolean hasStrongTransparency() {

        return transparentGrading != null
                && transparentGrading.isPositive();
    }

    public boolean hasExcellentStudentSupport() {

        return consultationSupport != null
                && consultationSupport.isPositive()
                && immediateFeedback != null
                && immediateFeedback.isPositive();
    }

    public boolean hasExcellentDiversityRecognition() {

        return diversityRecognition != null
                && diversityRecognition == RatingScale.ALWAYS_MANIFESTED;
    }

    public static int getTotalCriteria() {
        return 4;
    }
}