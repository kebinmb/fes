package com.faculty_evaluation_backend.fes.entities.primary;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "primary_faculty")
@AllArgsConstructor
@NoArgsConstructor
public class PrimaryFaculty extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primary_faculty_id")
    private Long primaryFacultyId;

    @Column(name = "faculty_id")
    private String facultyId;
    @Column(name = "lastname")
    private String lastname;
    @Column(name = "firstname")
    private String firstname;
    @Column(name = "position")
    private String position;
    @Column(name = "load_limit")
    private Double loadLimit;
    @Column(name = "middlename")
    private String middlename;

    @Column(name = "college")
    @Enumerated(EnumType.STRING)
    private College college;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;
}
