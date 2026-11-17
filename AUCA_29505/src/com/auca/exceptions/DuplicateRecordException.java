package com.auca.exceptions;

/** A unique ID, code, receipt number or active enrollment already exists. */
public class DuplicateRecordException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String message) {
        super(message);
    }

    public DuplicateRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
