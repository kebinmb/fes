package com.faculty_evaluation_backend.fes.entities.studentEvaluation;

import com.faculty_evaluation_backend.fes.entities.studentEvaluation.enums.RatingScale;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing faculty evaluation for Commitment and Transparency criteria
 * Uses RatingScale enum for type-safe rating values
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "commitment_and_transparency")
public class CommitmentAndTransparency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commitment_and_transparency_id")
    private Long commitmentAndTransparencyId;

    /**
     * Faculty being evaluated
     */
    @Column(name = "faculty_id", length = 60)
    private String facultyId;

    /**
     * Criterion 12: Recognizes and values the unique diversity and individual differences among students
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "diversity_recognition", nullable = false, length = 50)
    private RatingScale diversityRecognition;

    /**
     * Criterion 13: Assists students with their learning challenges during consultation hours
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_support", nullable = false, length = 50)
    private RatingScale consultationSupport;

    /**
     * Criterion 14: Provides immediate feedback on student outputs and performance
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "immediate_feedback", nullable = false, length = 50)
    private RatingScale immediateFeedback;

    /**
     * Criterion 15: Provides transparent and clear criteria in rating student's performance
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transparent_grading", nullable = false, length = 50)
    private RatingScale transparentGrading;

    /**
     * Calculate the average score for all commitment and transparency criteria
     * @return Average score (1.0 to 5.0)
     */
    public double calculateAverageScore() {
        if (diversityRecognition == null || consultationSupport == null ||
                immediateFeedback == null || transparentGrading == null) {
            return 0.0;
        }

        int totalScore = diversityRecognition.getScore() +
                consultationSupport.getScore() +
                immediateFeedback.getScore() +
                transparentGrading.getScore();

        return totalScore / 4.0;
    }

    /**
     * Get the overall verbal interpretation based on average score
     * @return Verbal interpretation (e.g., "Excellent", "Very Good", etc.)
     */
    public String getOverallInterpretation() {
        double average = calculateAverageScore();
        if (average >= 4.5) return "Excellent";
        if (average >= 3.5) return "Very Good";
        if (average >= 2.5) return "Good";
        if (average >= 1.5) return "Fair";
        return "Poor";
    }

    /**
     * Check if all criteria are rated positively (score >= 4)
     * @return true if all ratings are positive
     */
    public boolean isAllPositive() {
        return diversityRecognition.isPositive() &&
                consultationSupport.isPositive() &&
                immediateFeedback.isPositive() &&
                transparentGrading.isPositive();
    }

    /**
     * Check if all required ratings are present
     * @return true if all ratings are non-null
     */
    public boolean isComplete() {
        return diversityRecognition != null &&
                consultationSupport != null &&
                immediateFeedback != null &&
                transparentGrading != null;
    }

    /**
     * Check if transparency in grading is strong (score >= 4)
     * @return true if transparent grading rating is positive
     */
    public boolean hasStrongTransparency() {
        return transparentGrading != null && transparentGrading.isPositive();
    }

    /**
     * Check if student support is excellent (both consultation and feedback are positive)
     * @return true if both consultation support and immediate feedback are positive
     */
    public boolean hasExcellentStudentSupport() {
        return consultationSupport != null && consultationSupport.isPositive() &&
                immediateFeedback != null && immediateFeedback.isPositive();
    }

    /**
     * Check if diversity recognition is excellent (score == 5)
     * @return true if diversity recognition is always manifested
     */
    public boolean hasExcellentDiversityRecognition() {
        return diversityRecognition != null &&
                diversityRecognition == RatingScale.ALWAYS_MANIFESTED;
    }

    /**
     * Get total number of criteria
     * @return 4 (total criteria)
     */
    public static int getTotalCriteria() {
        return 4;
    }
}


