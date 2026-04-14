package com.faculty_evaluation_backend.fes.exceptions;

public class DuplicateEvaluationException extends RuntimeException {
    public DuplicateEvaluationException(String message) {
        super(message);
    }
}
