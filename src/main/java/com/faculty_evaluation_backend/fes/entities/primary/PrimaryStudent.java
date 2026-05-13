package com.faculty_evaluation_backend.fes.entities.primary;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.migration.entity.MigratableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "primary_student")
@AllArgsConstructor
@NoArgsConstructor
public class PrimaryStudent extends MigratableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primary_student_id")
    private Long primaryStudentId;

    @Column(name = "student_id")
    private String studentId;
    @Column(name = "curriculum_major_id")
    private Integer curriculumMajorId;
    @Column(name = "student_lastname")
    private String studentLastname;
    @Column(name = "student_firstname")
    private String studentFirstname;
    @Column(name = "student_middlename")
    private String studentMiddlename;
    @Column(name = "gender")
    private String gender;
}
