package com.faculty_evaluation_backend.fes.entities.primary;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyLoadStatus;
import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyWorkloadSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "faculty_workload",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_faculty_workload_term",
                        columnNames = {
                                "faculty_id",
                                "school_year",
                                "semester",
                                "class_code",
                                "course_code",
                                "program_code",
                                "year_level",
                                "section_code"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_faculty_workload_term",
                        columnList = "school_year, semester"
                ),
                @Index(
                        name = "idx_faculty_workload_status",
                        columnList = "load_status"
                ),
                @Index(
                        name = "idx_faculty_workload_course_section",
                        columnList = "course_code, program_code, year_level, section_code"
                )
        }
)
public class FacultyWorkload extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faculty_workload_id")
    private Long facultyWorkloadId;

    @Column(name = "faculty_id", nullable = false)
    private String facultyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "faculty_id",
            referencedColumnName = "faculty_id",
            insertable = false,
            updatable = false
    )
    private PrimaryFaculty faculty;

    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @Column(name = "semester", nullable = false)
    private String semester;

    @Column(name = "class_code")
    private String classCode;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "program_code", nullable = false)
    private String programCode;

    @Column(name = "year_level", nullable = false)
    private String yearLevel;

    @Column(name = "section_code", nullable = false)
    private String sectionCode;

    @Column(
            name = "total_hours_per_week",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal totalHoursPerWeek = BigDecimal.ZERO;

    @Column(name = "total_teaching_load", precision = 8, scale = 2)
    private BigDecimal totalTeachingLoad;

    @Column(name = "number_of_preparations")
    private Integer numberOfPreparations;

    @Column(name = "designation_etu", precision = 8, scale = 2)
    private BigDecimal designationEtu;

    @Column(name = "total_workload", precision = 8, scale = 2)
    private BigDecimal totalWorkload;

    @Column(
            name = "overload_hours",
            nullable = false,
            precision = 8,
            scale = 2
    )
    private BigDecimal overloadHours = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_status", nullable = false, length = 30)
    private FacultyLoadStatus loadStatus = FacultyLoadStatus.REGULAR_LOAD;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private FacultyWorkloadSource source = FacultyWorkloadSource.MANUAL;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @PrePersist
    @PreUpdate
    void syncLoadStatus() {
        if (totalHoursPerWeek == null) {
            totalHoursPerWeek = BigDecimal.ZERO;
        }

        if (overloadHours == null) {
            overloadHours = BigDecimal.ZERO;
        }

        loadStatus = overloadHours.compareTo(BigDecimal.ZERO) > 0
                ? FacultyLoadStatus.OVERLOAD
                : FacultyLoadStatus.REGULAR_LOAD;

        if (source == null) {
            source = FacultyWorkloadSource.MANUAL;
        }
    }
}
