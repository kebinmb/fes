package com.faculty_evaluation_backend.fes.entities.legacy;

import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacyStudentId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "student")
@Entity
public class LegacyStudent {
    @EmbeddedId
    private LegacyStudentId id;

    @Column(name = "curriculum_major_id")
    private Integer curriculumMajorId;
    @Column(name = "student_middlename")
    private String studentMiddlename;
    @Column(name = "gender")
    private String gender;
}
