package com.faculty_evaluation_backend.fes.entities.legacy;

import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacyStudentLoadId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student_load")
@Entity
public class LegacyStudentLoad {
    @EmbeddedId
    private LegacyStudentLoadId id;
}
