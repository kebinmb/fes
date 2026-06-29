package com.faculty_evaluation_backend.fes.entities.evaluation.enums;

import java.util.Arrays;
import java.util.List;

public enum EvaluationEvidenceCriterion {
    PUNCTUALITY("Management of Teaching and Learning", "Punctuality", "punctuality"),
    COURSE_CLARITY("Management of Teaching and Learning", "Course Clarity", "courseClarity"),
    TIME_MANAGEMENT("Management of Teaching and Learning", "Time Management", "timeManagement"),
    CRITICAL_THINKING_FACILITATION("Management of Teaching and Learning", "Critical Thinking Facilitation", "criticalThinkingFacilitation"),
    INDEPENDENT_LEARNING_GUIDANCE("Management of Teaching and Learning", "Independent Learning Guidance", "independentLearningGuidance"),
    FEEDBACK_COMMUNICATION("Management of Teaching and Learning", "Feedback Communication", "feedbackCommunication"),

    SUBJECT_KNOWLEDGE("Content Knowledge, Pedagogy and Technology", "Subject Knowledge", "subjectKnowledge"),
    CONTENT_SIMPLIFICATION("Content Knowledge, Pedagogy and Technology", "Content Simplification", "contentSimplification"),
    REAL_WORLD_APPLICATION("Content Knowledge, Pedagogy and Technology", "Real World Application", "realWorldApplication"),
    TECHNOLOGY_INTEGRATION("Content Knowledge, Pedagogy and Technology", "Technology Integration", "technologyIntegration"),
    ASSESSMENT_ALIGNMENT("Content Knowledge, Pedagogy and Technology", "Assessment Alignment", "assessmentAlignment"),

    DIVERSITY_RECOGNITION("Commitment and Transparency", "Diversity Recognition", "diversityRecognition"),
    CONSULTATION_SUPPORT("Commitment and Transparency", "Consultation Support", "consultationSupport"),
    IMMEDIATE_FEEDBACK("Commitment and Transparency", "Immediate Feedback", "immediateFeedback"),
    TRANSPARENT_GRADING("Commitment and Transparency", "Transparent Grading", "transparentGrading");

    private final String category;
    private final String label;
    private final String ratingKey;

    EvaluationEvidenceCriterion(String category, String label, String ratingKey) {
        this.category = category;
        this.label = label;
        this.ratingKey = ratingKey;
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public String getRatingKey() {
        return ratingKey;
    }

    public static EvaluationEvidenceCriterion from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Evidence criterion is required.");
        }

        return Arrays.stream(values())
                .filter(criterion ->
                        criterion.name().equalsIgnoreCase(value)
                                || criterion.ratingKey.equalsIgnoreCase(value)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid evidence criterion: " + value
                        )
                );
    }

    public static List<EvaluationEvidenceCriterion> list() {
        return List.of(values());
    }
}
