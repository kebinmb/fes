package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SubjectEvaluationDTO {
    // Context
    private String facultyId;
    private String evaluatorId;
    private String classCode;
    private String subjectCode;
    private String semester;
    private Integer schoolYear;
    private String accessCode;

    // Management of Teaching and Learning (Criteria 1-6)
    private String punctuality;
    private String courseClarity;
    private String timeManagement;
    private String criticalThinkingFacilitation;
    private String independentLearningGuidance;
    private String feedbackCommunication;

    // Content Knowledge, Pedagogy, and Technology (Criteria 7-11)
    private String subjectKnowledge;
    private String contentSimplification;
    private String realWorldApplication;
    private String technologyIntegration;
    private String assessmentAlignment;

    // Commitment and Transparency (Criteria 12-15)
    private String diversityRecognition;
    private String consultationSupport;
    private String immediateFeedback;
    private String transparentGrading;

    private String commentsOrFeedbacks;
}
