package com.faculty_evaluation_backend.fes.entities.evaluation;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing faculty evaluation for Management of Teaching and Learning criteria
 * Uses RatingScale enum for type-safe rating values
 */
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
    @JoinColumn(name = "faculty_id", referencedColumnName = "faculty_id", insertable = false, updatable = false)
    private PrimaryFaculty faculty;

    public double calculateAverageScore() {
        if (punctuality == null || courseClarity == null || timeManagement == null ||
            criticalThinkingFacilitation == null || independentLearningGuidance == null ||
            feedbackCommunication == null) {
            return 0.0;
        }

        int totalScore = punctuality.getScore() +
                        courseClarity.getScore() +
                        timeManagement.getScore() +
                        criticalThinkingFacilitation.getScore() +
                        independentLearningGuidance.getScore() +
                        feedbackCommunication.getScore();

        return totalScore / 6.0;
    }

    public String getOverallInterpretation() {
        double average = calculateAverageScore();
        if (average >= 4.5) return "Excellent";
        if (average >= 3.5) return "Very Good";
        if (average >= 2.5) return "Good";
        if (average >= 1.5) return "Fair";
        return "Poor";
    }

    public boolean isAllPositive() {
        return punctuality.isPositive() &&
               courseClarity.isPositive() &&
               timeManagement.isPositive() &&
               criticalThinkingFacilitation.isPositive() &&
               independentLearningGuidance.isPositive() &&
               feedbackCommunication.isPositive();
    }

    public boolean isComplete() {
        return punctuality != null &&
               courseClarity != null &&
               timeManagement != null &&
               criticalThinkingFacilitation != null &&
               independentLearningGuidance != null &&
               feedbackCommunication != null;
    }

    public static int getTotalCriteria() {
        return 6;
    }
}


