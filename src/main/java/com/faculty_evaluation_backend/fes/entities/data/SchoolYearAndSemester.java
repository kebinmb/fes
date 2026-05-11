package com.faculty_evaluation_backend.fes.entities.data;

import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "school_year_and_semester",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "school_year",
                                "semester"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolYearAndSemester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "school_year",
            nullable = false
    )
    private Integer schoolYear;

    @Column(
            name = "semester",
            nullable = false
    )
    private Semester semester;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false
    )
    private Status status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {

        this.createdAt = Instant.now();
    }
}