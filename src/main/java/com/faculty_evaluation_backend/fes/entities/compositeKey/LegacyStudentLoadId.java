package com.faculty_evaluation_backend.fes.entities.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegacyStudentLoadId implements Serializable {
    @Column(name = "load_id")
    private Integer loadId;
    @Column(name = "student_id")
    private String studentId;
    @Column(name = "yearlevel")
    private String yearLevel;
    @Column(name = "class_code")
    private Integer classCode;

    @Override
    public boolean equals(Object object){
        if (this == object)return true;
        if (object == null || getClass() != object.getClass()) return false;
        LegacyStudentLoadId that = (LegacyStudentLoadId) object;
        return Objects.equals(loadId, that.loadId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(yearLevel, that.yearLevel) &&
                Objects.equals(classCode, that.classCode);
    }

    @Override
    public int hashCode(){
        return Objects.hash(loadId, studentId, yearLevel, classCode);
    }
}
