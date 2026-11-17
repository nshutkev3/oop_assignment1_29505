package com.auca.exceptions;

/** No copy of a library book is available for borrowing. */
public class BookUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BookUnavailableException(String message) {
        super(message);
    }

    public BookUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
