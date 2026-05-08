package com.faculty_evaluation_backend.fes.entities.sis_authentication;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "student_user_a")
public class StudentUserA extends BaseStudentAccount {
}