package com.faculty_evaluation_backend.fes.entities.data;

import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SemesterConverter
        implements AttributeConverter<Semester, String> {

    @Override
    public String convertToDatabaseColumn(
            Semester semester
    ) {

        if (semester == null) {

            return null;
        }

        return semester.getValue();
    }

    @Override
    public Semester convertToEntityAttribute(
            String dbValue
    ) {

        if (dbValue == null) {

            return null;
        }

        return Semester.fromValue(dbValue);
    }
}
