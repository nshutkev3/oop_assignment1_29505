package com.auca.exceptions;

/** A prerequisite record (Department, Teacher, Student, ...) is missing and was not created. */
public class PrerequisiteMissingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PrerequisiteMissingException(String message) {
        super(message);
    }

    public PrerequisiteMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
