package com.faculty_evaluation_backend.fes.entities.primary;

import com.faculty_evaluation_backend.fes.audit.Auditable;
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
public class PrimaryStudentLoad extends Auditable {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_code", referencedColumnName = "class_code", insertable = false, updatable = false)
    private PrimaryClass primaryClass;

    @Column(name = "year_level")
    private String yearLevel;
    @Column(name = "class_code")
    private String classCode;

    @Column(name = "grade")
    private String grade;
}
