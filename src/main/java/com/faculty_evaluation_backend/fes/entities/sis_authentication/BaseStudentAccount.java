package com.faculty_evaluation_backend.fes.entities.sis_authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseStudentAccount {

    @Id
    @Column(name = "student_user_id")
    private Integer studentUserId;

    @Column(name = "student_id", nullable = false, length = 15)
    private String studentId;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", length = 45)
    private String email;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "contact_number", length = 255)
    private String contactNumber;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "status")
    private Integer status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "campus")
    private Integer campus;
}
