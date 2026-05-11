package com.faculty_evaluation_backend.fes.entities.data.enums;

public enum Semester {

    FIRST("1st"),

    SECOND("2nd"),

    SUMMER("summer");

    private final String value;

    Semester(String value) {

        this.value = value;
    }

    public String getValue() {

        return value;
    }

    public static Semester fromValue(
            String value
    ) {

        for (Semester semester : values()) {

            if (
                    semester.value.equalsIgnoreCase(value)
            ) {

                return semester;
            }
        }

        throw new IllegalArgumentException(
                "Invalid semester value: " + value
        );
    }
}