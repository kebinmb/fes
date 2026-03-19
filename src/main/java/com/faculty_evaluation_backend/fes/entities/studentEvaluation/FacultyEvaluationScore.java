package com.faculty_evaluation_backend.fes.entities.studentEvaluation;

import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * Entity representing the complete faculty evaluation score
 * Aggregates all three evaluation categories:
 * - Management of Teaching and Learning
 * - Content Knowledge, Pedagogy, and Technology
 * - Commitment and Transparency
 *
 * One FacultyEvaluationScore per student per faculty per evaluation period
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "faculty_evaluation_score",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"faculty_id", "student_id", "class_code", "semester", "school_year"}
       ))
public class FacultyEvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faculty_evaluation_score_id")
    private Long facultyEvaluationScoreId;

    // ==================== Faculty Being Evaluated ====================

    /**
     * Faculty being evaluated (stored as string for flexibility)
     */
    @Column(name = "faculty_id", nullable = false, length = 60)
    private String facultyId;

    /**
     * Optional: Relationship to PrimaryFaculty entity
     * Uses insertable=false, updatable=false to avoid column mapping conflicts
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", referencedColumnName = "faculty_id",
                insertable = false, updatable = false)
    private PrimaryFaculty faculty;

    // ==================== Student Evaluator ====================

    /**
     * Student/Supervisor who submitted this evaluation
     */
    @Column(name = "evaluator_id", nullable = false, length = 15)
    private String evaluatorId;

    /**
     * Optional: Relationship to PrimaryStudent entity
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id",
                insertable = false, updatable = false)
    private PrimaryStudent student;

    // ==================== Evaluation Context ====================

    /**
     * Class code for which this evaluation is submitted
     */
    @Column(name = "class_code", nullable = false, length = 20)
    private String classCode;

    /**
     * Semester of evaluation (e.g., "1st", "2nd")
     */
    @Column(name = "semester", nullable = false, length = 10)
    private String semester;

    /**
     * School year of evaluation (e.g., 2023, 2024)
     */
    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @Column(name = "subject_code",nullable = false)
    private String subjectCode;

    @Column(name = "comments_or_feedbacks")
    private String commentsOrFeedbacks;

    // ==================== Evaluation Categories (One-to-One) ====================

    /**
     * Management of Teaching and Learning evaluation (Criteria 1-6)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "management_of_teaching_and_learning_id",
                referencedColumnName = "management_of_teaching_and_learning_id")
    private ManagementOfTeachingAndLearning managementOfTeachingAndLearning;

    /**
     * Content Knowledge, Pedagogy, and Technology evaluation (Criteria 7-11)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_knowledge_pedagogy_and_technology_id",
                referencedColumnName = "content_knowledge_pedagogy_and_technology_id")
    private ContentKnowledgePedagogyAndTechnology contentKnowledgePedagogyAndTechnology;

    /**
     * Commitment and Transparency evaluation (Criteria 12-15)
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "commitment_and_transparency_id",
                referencedColumnName = "commitment_and_transparency_id")
    private CommitmentAndTransparency commitmentAndTransparency;

    // ==================== Metadata ====================

    /**
     * Overall average score (calculated from all three categories)
     */
    @Column(name = "overall_average_score")
    private Double overallAverageScore;

    /**
     * Overall verbal interpretation
     */
    @Column(name = "overall_interpretation", length = 20)
    private String overallInterpretation;

    /**
     * Evaluation submission timestamp
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== Business Logic Methods ====================

    /**
     * Calculate and update the overall average score from all three categories
     * Should be called after all evaluation categories are set
     */
    public void calculateOverallScore() {
        if (managementOfTeachingAndLearning == null ||
            contentKnowledgePedagogyAndTechnology == null ||
            commitmentAndTransparency == null) {
            this.overallAverageScore = 0.0;
            this.overallInterpretation = "Incomplete";
            return;
        }

        double teachingScore = managementOfTeachingAndLearning.calculateAverageScore();
        double contentScore = contentKnowledgePedagogyAndTechnology.calculateAverageScore();
        double commitmentScore = commitmentAndTransparency.calculateAverageScore();

        // Calculate weighted average (equal weights for all three categories)
        this.overallAverageScore = (teachingScore + contentScore + commitmentScore) / 3.0;

        // Set overall interpretation
        this.overallInterpretation = determineOverallInterpretation(this.overallAverageScore);
    }

    /**
     * Determine verbal interpretation based on overall average score
     */
    private String determineOverallInterpretation(double average) {
        if (average >= 4.5) return "Excellent";
        if (average >= 3.5) return "Very Good";
        if (average >= 2.5) return "Good";
        if (average >= 1.5) return "Fair";
        return "Poor";
    }

    /**
     * Check if all evaluation categories are complete
     */
    public boolean isComplete() {
        return managementOfTeachingAndLearning != null &&
               managementOfTeachingAndLearning.isComplete() &&
               contentKnowledgePedagogyAndTechnology != null &&
               contentKnowledgePedagogyAndTechnology.isComplete() &&
               commitmentAndTransparency != null &&
               commitmentAndTransparency.isComplete();
    }

    /**
     * Check if all evaluations are positive (score >= 4)
     */
    public boolean isAllPositive() {
        return managementOfTeachingAndLearning != null &&
               managementOfTeachingAndLearning.isAllPositive() &&
               contentKnowledgePedagogyAndTechnology != null &&
               contentKnowledgePedagogyAndTechnology.isAllPositive() &&
               commitmentAndTransparency != null &&
               commitmentAndTransparency.isAllPositive();
    }

    /**
     * Get total number of criteria across all categories
     */
    public static int getTotalCriteria() {
        return ManagementOfTeachingAndLearning.getTotalCriteria() +
               ContentKnowledgePedagogyAndTechnology.getTotalCriteria() +
               CommitmentAndTransparency.getTotalCriteria();
    }

    /**
     * Get breakdown of scores by category
     */
    public String getScoreBreakdown() {
        if (!isComplete()) {
            return "Evaluation incomplete";
        }

        return String.format(
            "Teaching & Learning: %.2f | Content & Technology: %.2f | Commitment & Transparency: %.2f | Overall: %.2f (%s)",
            managementOfTeachingAndLearning.calculateAverageScore(),
            contentKnowledgePedagogyAndTechnology.calculateAverageScore(),
            commitmentAndTransparency.calculateAverageScore(),
            overallAverageScore,
            overallInterpretation
        );
    }

    /**
     * Check if faculty passed the evaluation (overall average >= 3.0)
     */
    public boolean hasPassed() {
        return overallAverageScore != null && overallAverageScore >= 3.0;
    }

    /**
     * Lifecycle hook: Calculate overall score before persisting
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        calculateOverallScore();
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
}
