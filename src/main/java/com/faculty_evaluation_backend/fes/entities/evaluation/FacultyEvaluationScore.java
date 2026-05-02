package com.faculty_evaluation_backend.fes.entities.evaluation;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;


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
public class FacultyEvaluationScore extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faculty_evaluation_score_id")
    private Long facultyEvaluationScoreId;

    @Column(name = "faculty_id", nullable = false, length = 60)
    private String facultyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", referencedColumnName = "faculty_id",
                insertable = false, updatable = false)
    private PrimaryFaculty faculty;

    @Column(name = "evaluator_id", nullable = false, length = 15)
    private String evaluatorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id",
                insertable = false, updatable = false)
    private PrimaryStudent student;

    @Column(name = "class_code", nullable = false, length = 20)
    private String classCode;

    @Column(name = "semester", nullable = false, length = 10)
    private String semester;

    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @Column(name = "subject_code",nullable = false)
    private String subjectCode;

    @Column(name = "year_level", nullable = false)
    private String yearLevel;

    @Column(name = "comments_or_feedbacks")
    private String commentsOrFeedbacks;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "management_of_teaching_and_learning_id",
                referencedColumnName = "management_of_teaching_and_learning_id")
    private ManagementOfTeachingAndLearning managementOfTeachingAndLearning;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "content_knowledge_pedagogy_and_technology_id",
                referencedColumnName = "content_knowledge_pedagogy_and_technology_id")
    private ContentKnowledgePedagogyAndTechnology contentKnowledgePedagogyAndTechnology;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "commitment_and_transparency_id",
                referencedColumnName = "commitment_and_transparency_id")
    private CommitmentAndTransparency commitmentAndTransparency;

    @Column(name = "overall_average_score")
    private Double overallAverageScore;

    @Column(name = "overall_interpretation", length = 20)
    private String overallInterpretation;


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

        this.overallAverageScore = (teachingScore + contentScore + commitmentScore) / 3.0;

        this.overallInterpretation = determineOverallInterpretation(this.overallAverageScore);
    }

    private String determineOverallInterpretation(double average) {
        if (average >= 4.5) return "Excellent";
        if (average >= 3.5) return "Very Good";
        if (average >= 2.5) return "Good";
        if (average >= 1.5) return "Fair";
        return "Poor";
    }

    public boolean isComplete() {
        return managementOfTeachingAndLearning != null &&
               managementOfTeachingAndLearning.isComplete() &&
               contentKnowledgePedagogyAndTechnology != null &&
               contentKnowledgePedagogyAndTechnology.isComplete() &&
               commitmentAndTransparency != null &&
               commitmentAndTransparency.isComplete();
    }

    public boolean isAllPositive() {
        return managementOfTeachingAndLearning != null &&
               managementOfTeachingAndLearning.isAllPositive() &&
               contentKnowledgePedagogyAndTechnology != null &&
               contentKnowledgePedagogyAndTechnology.isAllPositive() &&
               commitmentAndTransparency != null &&
               commitmentAndTransparency.isAllPositive();
    }

    public static int getTotalCriteria() {
        return ManagementOfTeachingAndLearning.getTotalCriteria() +
               ContentKnowledgePedagogyAndTechnology.getTotalCriteria() +
               CommitmentAndTransparency.getTotalCriteria();
    }

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

    public boolean hasPassed() {
        return overallAverageScore != null && overallAverageScore >= 3.0;
    }

//    @PrePersist
//    @PreUpdate
//    public void prePersist() {
//        calculateOverallScore();
//        if (createdAt == null) {
//            createdAt = Instant.now();
//        }
//        updatedAt = Instant.now();
//    }
}
