package com.faculty_evaluation_backend.fes.entities.primary;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "primary_student_load")
@Entity
public class PrimaryStudentLoad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primary_student_load_id")
    private Long primaryStudentLoadId;

    @Column(name = "load_id")
    private Integer loadId;
    @Column(name = "student_id")
    private String studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", insertable = false, updatable = false)
    private PrimaryStudent student;

    @Column(name = "year_level")
    private String yearLevel;
    @Column(name = "class_code")
    private Integer classCode;

    @Column(name = "grade")
    private String grade;
}
