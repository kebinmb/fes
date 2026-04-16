package com.faculty_evaluation_backend.fes.entities.data;

import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchoolYearAndSemester {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_year")
    private Integer schoolYear;
    @Column(name = "semester")
    private String semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
}
