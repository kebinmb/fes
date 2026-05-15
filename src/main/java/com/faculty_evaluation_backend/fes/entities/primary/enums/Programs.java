package com.faculty_evaluation_backend.fes.entities.primary.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Programs {

    TCP("TCP"),
    SUMMER("Summer"),
    SPECIAL("Special"),
    SP("SP"),
    SHS("SHS"),
    ROTC("ROTC"),
    REGISTRAR("Registrar"),
    PRESCHOOL("PRESCHOOL"),

    PHD("Ph.D ."),

    OCTOBERIAN("Octoberian"),

    MTM("MTM"),

    MPA_DOT("MPA ."),
    MPA("MPA"),

    MFT("MFT"),

    MBA("MBA"),

    MAT_TLE("MAT-TLE"),
    MAT_MATH("MAT-MATH"),
    MAT_GEN_SCI("MAT-Gen Sci"),
    MAT_ENG("MAT-Eng"),
    MAT("MAT"),

    MAED_AT("MAED-AT"),
    MAED("MAED"),

    HELE("HELE"),

    GRADUATE("GRADUATE"),

    G11("G11"),

    EDD("Ed.D ."),

    ECP_BLG("ECP-BLG"),

    DPA("DPA"),

    DOED("DOED"),

    BTVTED("BTVTEd"),

    BTTE("BTTE"),

    BTLED("BTLED"),

    BSOA("BSOA"),

    BSNED("BSNED"),

    BSMA("BSMA"),

    BSIT("BSIT"),

    BSIS("BSIS"),

    BSIND("BSIND"),

    BSHRM("BSHRM"),

    BSHM("BSHM"),

    BSFI("BSFI"),

    BSED("BSED"),

    BSE("BSE"),

    BSCRIM("BSCRIM"),

    BSCE("BSCE"),

    BSBCRIM("BSBCRIM"),

    BSBA_FM("BSBA-FM"),

    BSBA("BSBA"),

    BSAM("BSAM"),

    BSACT("BSACT"),

    BSA("BSA"),

    BS_PSYCH("BS PSYCH"),

    BS_IND_TECH("BS IND TECH"),

    BS_ECE("BS ECE"),

    BS_CPE("BS CPE"),

    BPED("BPED"),

    BPA("BPA"),

    BIT("BIT"),

    BEED("BEED"),

    BECED("BECED"),

    AIT("AIT"),

    ADVISING("ADVISING"),

    AB_SOCSCI("AB-SOCSCI"),

    AB_ENG_L("AB-ENG L"),

    AB_ENG("AB-ENG");

    private final String value;

    public static Programs fromValue(String value) {
        for (Programs program : Programs.values()) {
            if (program.value.equalsIgnoreCase(value)) {
                return program;
            }
        }

        throw new IllegalArgumentException("Unknown program value: " + value);
    }
}