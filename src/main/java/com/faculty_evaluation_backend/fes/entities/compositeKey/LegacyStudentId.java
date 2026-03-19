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
public class LegacyStudentId implements Serializable {
    @Column(name = "student_id")
    private String studentId;
    @Column(name = "student_lastname")
    private String studentLastname;
    @Column(name = "student_firstname")
    private String studentFirstname;

    @Override
    public boolean equals(Object object){
        if (this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        LegacyStudentId that = (LegacyStudentId) object;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(studentLastname, that.studentLastname) &&
                Objects.equals(studentFirstname, that.studentFirstname);
    }
    @Override
    public int hashCode(){
        return Objects.hash(studentId, studentLastname, studentFirstname);
    }
}
