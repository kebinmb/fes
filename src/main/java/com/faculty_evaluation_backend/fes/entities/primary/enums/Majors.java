package com.faculty_evaluation_backend.fes.entities.primary.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Majors {

    BSED_ENG("BSED Eng"),

    BSED_FIL("BSED Fil"),

    BSED_FILIPINO("BSED Filipino"),

    BSED_MATH("BSED Math"),

    BSED_SCI("BSED Sci"),

    BSED_SP_FIL_2("BSED SP FIL 2"),

    BSED_TLE_SPC_SUMMER("BSED TLE SPC - SUMMER"),

    BSED4ASP("BSED4ASP"),

    NONE("None");

    private final String databaseValue;

    @JsonValue
    public String getDisplayValue() {
        return this.name();
    }
}