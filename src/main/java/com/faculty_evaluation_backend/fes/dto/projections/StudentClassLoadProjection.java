package com.faculty_evaluation_backend.fes.dto.projections;

public interface StudentClassLoadProjection {

    String getClassCode();

    String getFacultyId();

    String getSubjectCode();

    Integer getSectionId();

    String getSemester();

    Integer getSchoolYear();

    String getYearLevel();

    String getStudentId();

    String getCollege();

    String getFacultyName();

    String getSubjectDescription();
}
