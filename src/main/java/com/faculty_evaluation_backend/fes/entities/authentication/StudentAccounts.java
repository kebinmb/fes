package com.faculty_evaluation_backend.fes.entities.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "student_accounts", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_email", columnList = "email")
})
@EntityListeners(AuditingEntityListener.class)
public class StudentAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_account_id")
    private Long studentAccountId;

    // Reference to PrimaryStudent (NO JPA relationship - just store the ID)
    @EqualsAndHashCode.Include
    @Column(name = "student_id", nullable = false, unique = true, length = 15)
    private String studentId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Relationship to access codes
    @JsonIgnore
    @OneToMany(mappedBy = "studentAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudentAccessCode> accessCodes = new ArrayList<>();

    // Helper methods
    public void addAccessCode(StudentAccessCode accessCode) {
        accessCodes.add(accessCode);
        accessCode.setStudentAccount(this);
        accessCode.setStudentId(this.studentId); // Ensure consistency
    }

    public void removeAccessCode(StudentAccessCode accessCode) {
        accessCodes.remove(accessCode);
        accessCode.setStudentAccount(null);
    }
}
