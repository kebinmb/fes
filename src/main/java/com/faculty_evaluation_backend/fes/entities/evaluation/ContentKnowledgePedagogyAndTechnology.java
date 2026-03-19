package com.faculty_evaluation_backend.fes.entities.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing faculty evaluation for Content Knowledge, Pedagogy, and Technology criteria
 * Uses RatingScale enum for type-safe rating values
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "content_knowledge_pedagogy_and_technology")
public class ContentKnowledgePedagogyAndTechnology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_knowledge_pedagogy_and_technology_id")
    private Long contentKnowledgePedagogyAndTechnologyId;

    /**
     * Faculty being evaluated
     */
    @Column(name = "faculty_id", length = 60)
    private String facultyId;

    /**
     * Criterion 7: Demonstrates extensive and broad knowledge of the subject/course
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "subject_knowledge", nullable = false, length = 50)
    private RatingScale subjectKnowledge;

    /**
     * Criterion 8: Simplifies complex ideas in the lesson for ease of understanding
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "content_simplification", nullable = false, length = 50)
    private RatingScale contentSimplification;

    /**
     * Criterion 9: Relates the subject matter to contemporary issues and developments
     * in the discipline and/or daily life activities
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "real_world_application", nullable = false, length = 50)
    private RatingScale realWorldApplication;

    /**
     * Criterion 10: Promotes active learning and student engagement by using appropriate
     * teaching and learning resources including ICT tools and platforms
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "technology_integration", nullable = false, length = 50)
    private RatingScale technologyIntegration;

    /**
     * Criterion 11: Uses appropriate assessments (projects, exams, quizzes, assignments, etc.)
     * aligned with the learning outcomes
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_alignment", nullable = false, length = 50)
    private RatingScale assessmentAlignment;

    /**
     * Calculate the average score for all content knowledge, pedagogy, and technology criteria
     * @return Average score (1.0 to 5.0)
     */
    public double calculateAverageScore() {
        if (subjectKnowledge == null || contentSimplification == null || realWorldApplication == null ||
            technologyIntegration == null || assessmentAlignment == null) {
            return 0.0;
        }

        int totalScore = subjectKnowledge.getScore() +
                        contentSimplification.getScore() +
                        realWorldApplication.getScore() +
                        technologyIntegration.getScore() +
                        assessmentAlignment.getScore();

        return totalScore / 5.0;
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
        return subjectKnowledge.isPositive() &&
               contentSimplification.isPositive() &&
               realWorldApplication.isPositive() &&
               technologyIntegration.isPositive() &&
               assessmentAlignment.isPositive();
    }

    /**
     * Check if all required ratings are present
     * @return true if all ratings are non-null
     */
    public boolean isComplete() {
        return subjectKnowledge != null &&
               contentSimplification != null &&
               realWorldApplication != null &&
               technologyIntegration != null &&
               assessmentAlignment != null;
    }

    /**
     * Check if technology integration is strong (score >= 4)
     * @return true if technology integration rating is positive
     */
    public boolean hasStrongTechnologyIntegration() {
        return technologyIntegration != null && technologyIntegration.isPositive();
    }

    /**
     * Check if subject knowledge is excellent (score == 5)
     * @return true if subject knowledge is always manifested
     */
    public boolean hasExcellentSubjectKnowledge() {
        return subjectKnowledge != null &&
               subjectKnowledge == RatingScale.ALWAYS_MANIFESTED;
    }

    /**
     * Get total number of criteria
     * @return 5 (total criteria)
     */
    public static int getTotalCriteria() {
        return 5;
    }
}
