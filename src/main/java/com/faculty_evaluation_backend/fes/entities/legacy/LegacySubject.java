package com.faculty_evaluation_backend.fes.entities.legacy;


import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacySubjectId;
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
@Table(name = "subject")
@Entity
public class LegacySubject {
    @EmbeddedId
    private LegacySubjectId id;
}
