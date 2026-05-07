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
@Table(name = "management_of_teaching_and_learning")
public class ManagementOfTeachingAndLearning extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "management_of_teaching_and_learning_id")
    private Long managementOfTeachingAndLearningId;

    @Column(name = "faculty_id", length = 60)
    private String facultyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "punctuality", nullable = false, length = 50)
    private RatingScale punctuality;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_clarity", nullable = false, length = 50)
    private RatingScale courseClarity;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_management", nullable = false, length = 50)
    private RatingScale timeManagement;

    @Enumerated(EnumType.STRING)
    @Column(name = "critical_thinking_facilitation", nullable = false, length = 50)
    private RatingScale criticalThinkingFacilitation;

    @Enumerated(EnumType.STRING)
    @Column(name = "independent_learning_guidance", nullable = false, length = 50)
    private RatingScale independentLearningGuidance;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_communication", nullable = false, length = 50)
    private RatingScale feedbackCommunication;

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

        return punctuality.getScore()
                + courseClarity.getScore()
                + timeManagement.getScore()
                + criticalThinkingFacilitation.getScore()
                + independentLearningGuidance.getScore()
                + feedbackCommunication.getScore();
    }

    public double calculatePercentageScore() {

        // MAX = 6 criteria × 5 = 30
        return (calculateAverageScore() / 30.0) * 100.0;
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

        return punctuality.isPositive()
                && courseClarity.isPositive()
                && timeManagement.isPositive()
                && criticalThinkingFacilitation.isPositive()
                && independentLearningGuidance.isPositive()
                && feedbackCommunication.isPositive();
    }

    public boolean isComplete() {

        return punctuality != null
                && courseClarity != null
                && timeManagement != null
                && criticalThinkingFacilitation != null
                && independentLearningGuidance != null
                && feedbackCommunication != null;
    }

    public static int getTotalCriteria() {
        return 6;
    }
}