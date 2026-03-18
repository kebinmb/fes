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
public class LegacyFacultyId implements Serializable {
    @Column(name = "faculty_id")
    private String facultyId;
    @Column(name = "lastname")
    private String lastname;
    @Column(name = "firstname")
    private String firstname;

    @Override
    public boolean equals(Object object){
        if (this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        LegacyFacultyId that = (LegacyFacultyId) object;
        return Objects.equals(facultyId, that.facultyId) &&
                Objects.equals(lastname, that.lastname) &&
                Objects.equals(firstname, that.firstname);
    }
    @Override
    public int hashCode(){
        return Objects.hash(facultyId, lastname, firstname);
    }
}
