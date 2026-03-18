package com.faculty_evaluation_backend.fes.entities.compositeKey;

import com.faculty_evaluation_backend.fes.entities.legacy.LegacySection;
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
@AllArgsConstructor
@NoArgsConstructor
public class LegacySectionId implements Serializable {
    @Column(name = "section_id")
    private Integer sectionId;
    @Column(name = "program_code")
    private String programCode;

    @Override
    public boolean equals(Object object){
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LegacySectionId that = (LegacySectionId) object;
        return Objects.equals(sectionId, that.sectionId)  &&
                Objects.equals(programCode, that.programCode);
    }

    @Override
    public int hashCode(){
        return Objects.hash(sectionId,programCode);
    }
}
