package com.faculty_evaluation_backend.fes.entities.studentEvaluation.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Rating scale for faculty evaluation
 * Represents the frequency/quality of faculty performance manifestation
 */
@Getter
public enum RatingScale {
    ALWAYS_MANIFESTED(5, "Always Manifested", "The faculty consistently demonstrates this behavior/quality", "AM"),
    OFTEN_MANIFESTED(4, "Often Manifested", "The faculty frequently demonstrates this behavior/quality", "OM"),
    SOMETIMES_MANIFESTED(3, "Sometimes Manifested", "The faculty occasionally demonstrates this behavior/quality", "SM"),
    SELDOM_MANIFESTED(2, "Seldom Manifested", "The faculty rarely demonstrates this behavior/quality", "SLM"),
    NEVER_OR_RARELY_MANIFESTED(1, "Never or Rarely Manifested", "The faculty never or almost never demonstrates this behavior/quality", "NRM");

    private final int score;
    private final String displayName;
    private final String description;
    private final String code;

    RatingScale(int score, String displayName, String description, String code) {
        this.score = score;
        this.displayName = displayName;
        this.description = description;
        this.code = code;
    }

    /**
     * Get the display name for JSON serialization
     * @return The human-readable display name
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get RatingScale from numeric score
     * @param score The numeric score (1-5)
     * @return The corresponding RatingScale
     * @throws IllegalArgumentException if score is invalid
     */
    public static RatingScale fromScore(int score) {
        for (RatingScale scale : values()) {
            if (scale.score == score) {
                return scale;
            }
        }
        throw new IllegalArgumentException("Invalid rating score: " + score + ". Must be between 1 and 5.");
    }

    /**
     * Get RatingScale from display name (case-insensitive)
     * @param displayName The display name
     * @return The corresponding RatingScale
     * @throws IllegalArgumentException if display name is invalid
     */
    public static RatingScale fromDisplayName(String displayName) {
        for (RatingScale scale : values()) {
            if (scale.displayName.equalsIgnoreCase(displayName)) {
                return scale;
            }
        }
        throw new IllegalArgumentException("Invalid rating display name: " + displayName);
    }

    /**
     * Get RatingScale from code or enum name (case-insensitive)
     * Accepts both short codes (AM, OM, SM, SLM, NRM) and full enum names (ALWAYS_MANIFESTED, etc.)
     * @param code The rating code or enum name
     * @return The corresponding RatingScale
     * @throws IllegalArgumentException if code is invalid
     */
    public static RatingScale fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Rating code cannot be null or empty");
        }

        String trimmedCode = code.trim();

        // First, try to match by enum name (ALWAYS_MANIFESTED, OFTEN_MANIFESTED, etc.)
        try {
            return RatingScale.valueOf(trimmedCode.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If enum name doesn't match, try short code
        }

        // Try to match by short code (AM, OM, SM, SLM, NRM)
        for (RatingScale scale : values()) {
            if (scale.code.equalsIgnoreCase(trimmedCode)) {
                return scale;
            }
        }

        // If neither works, throw exception with helpful message
        throw new IllegalArgumentException(
                "Invalid rating code: " + code + ". " +
                        "Valid values are: ALWAYS_MANIFESTED (or AM), OFTEN_MANIFESTED (or OM), " +
                        "SOMETIMES_MANIFESTED (or SM), SELDOM_MANIFESTED (or SLM), " +
                        "NEVER_OR_RARELY_MANIFESTED (or NRM)"
        );
    }

    /**
     * Check if the rating is positive (score >= 4)
     * @return true if rating is positive
     */
    public boolean isPositive() {
        return score >= 4;
    }

    /**
     * Check if the rating is negative (score <= 2)
     * @return true if rating is negative
     */
    public boolean isNegative() {
        return score <= 2;
    }

    /**
     * Check if the rating is neutral (score == 3)
     * @return true if rating is neutral
     */
    public boolean isNeutral() {
        return score == 3;
    }

    /**
     * Get the minimum passing score
     * @return The minimum score considered passing (3)
     */
    public static int getMinimumPassingScore() {
        return 3;
    }

    /**
     * Get the maximum possible score
     * @return The maximum score (5)
     */
    public static int getMaximumScore() {
        return 5;
    }

    /**
     * Calculate percentage from score
     * @return Percentage representation (0-100)
     */
    public double getPercentage() {
        return (score / 5.0) * 100;
    }

    /**
     * Get a verbal interpretation of the rating
     * @return Verbal interpretation (e.g., "Excellent", "Good", etc.)
     */
    public String getVerbalInterpretation() {
        return switch (this) {
            case ALWAYS_MANIFESTED -> "Excellent";
            case OFTEN_MANIFESTED -> "Very Good";
            case SOMETIMES_MANIFESTED -> "Good";
            case SELDOM_MANIFESTED -> "Fair";
            case NEVER_OR_RARELY_MANIFESTED -> "Poor";
        };
    }

    @Override
    public String toString() {
        return String.format("%s (%d) - %s", displayName, score, code);
    }
}
