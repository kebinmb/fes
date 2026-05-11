package com.faculty_evaluation_backend.fes.entities.data.enums;

public enum Semester {

    FIRST_SEMESTER("1st"),

    SECOND_SEMESTER("2nd"),

    SUMMER_SEMESTER("summer");

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