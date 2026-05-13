package com.faculty_evaluation_backend.fes.entities.primary;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.migration.entity.MigratableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "primary_class")
public class PrimaryClass extends MigratableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primary_class_id")
    private Long primaryClassId;

    @Column(name = "class_code")
    private String classCode;
    @Column(name = "faculty_id")
    private String facultyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", referencedColumnName = "faculty_id", insertable = false, updatable = false)
    private PrimaryFaculty faculty;
    @Column(name = "subject_code")
    private String subjectCode;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_code", referencedColumnName = "subject_code", insertable = false, updatable = false)
    private PrimarySubject subject;
    @Column(name = "section_id")
    private Integer sectionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", referencedColumnName = "section_id", insertable = false, updatable = false)
    private PrimarySection section;
    @Column(name = "semester")
    private String semester;
    @Column(name = "school_year")
    private Integer schoolYear;
    @Column(name = "schedule_day")
    private String scheduleDay;
    @Column(name = "schedule_time")
    private String scheduleTime;
    @Column(name = "room")
    private String room;
}
