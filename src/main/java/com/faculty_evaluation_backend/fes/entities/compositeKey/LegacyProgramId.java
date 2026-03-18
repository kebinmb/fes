package com.faculty_evaluation_backend.fes.entities.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegacyProgramId implements Serializable {
    @Column(name = "program_code")
    private String programCode;
    @Column(name = "program_title")
    private String programTitle;

    @Override
    public boolean equals(Object object){
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LegacyProgramId that = (LegacyProgramId) object;
        return Objects.equals(programCode, that.programCode) &&
                Objects.equals(programTitle, that.programTitle);
    }
    @Override
    public int hashCode() {
        return Objects.hash(programCode, programTitle);
    }
}
