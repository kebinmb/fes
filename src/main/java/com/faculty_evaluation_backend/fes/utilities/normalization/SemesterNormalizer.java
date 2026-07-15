package com.faculty_evaluation_backend.fes.utilities.normalization;

import java.util.Locale;

public final class SemesterNormalizer {
    public static final String FIRST_SEMESTER_VALUE = "1st";
    public static final String SECOND_SEMESTER_VALUE = "2nd";
    public static final String SUMMER_SEMESTER_VALUE = "summer";

    private SemesterNormalizer() {
    }

    public static String toCanonicalValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = normalizeToken(value);

        return switch (normalized) {
            case "1ST", "FIRST", "FIRSTSEMESTER" -> FIRST_SEMESTER_VALUE;
            case "2ND", "SECOND", "SECONDSEMESTER" -> SECOND_SEMESTER_VALUE;
            case "SUMMER", "SUMMERSEMESTER" -> SUMMER_SEMESTER_VALUE;
            default -> value.trim();
        };
    }

    public static String toEnumName(String value) {
        String canonicalValue = toCanonicalValue(value);

        if (canonicalValue == null) {
            return null;
        }

        return switch (canonicalValue.toLowerCase(Locale.ROOT)) {
            case FIRST_SEMESTER_VALUE -> "FIRST_SEMESTER";
            case SECOND_SEMESTER_VALUE -> "SECOND_SEMESTER";
            case SUMMER_SEMESTER_VALUE -> "SUMMER_SEMESTER";
            default -> canonicalValue;
        };
    }

    public static boolean isKnownSemester(String value) {
        String canonicalValue = toCanonicalValue(value);

        return FIRST_SEMESTER_VALUE.equalsIgnoreCase(canonicalValue)
                || SECOND_SEMESTER_VALUE.equalsIgnoreCase(canonicalValue)
                || SUMMER_SEMESTER_VALUE.equalsIgnoreCase(canonicalValue);
    }

    private static String normalizeToken(String value) {
        return value.trim()
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
    }
}
