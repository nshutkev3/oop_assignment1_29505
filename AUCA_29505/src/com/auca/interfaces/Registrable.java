package com.auca.interfaces;

import com.auca.exceptions.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contract for every entity that can be validated, printed and summarised on registration.
 * The static helpers below are shared validation rules used by all entity classes and by Main
 * (static interface methods are allowed by the assignment: "default/static").
 */
public interface Registrable {
    int MIN_NAME_LENGTH = 2;
    int MAX_NAME_LENGTH = 60;
    int MAX_EMAIL_LENGTH = 100;
    int MIN_PHONE_LENGTH = 10;
    int MAX_PHONE_LENGTH = 15;
    String DATE_PATTERN = "yyyy-MM-dd";

    void validate() throws ValidationException;

    void printDetails();

    String getRegistrationSummary();

    // ------------------------------------------------------------------ shared rules

    /** Required, non-blank text whose trimmed length is within [min, max]. Returns the trimmed value. */
    static String requireText(String field, String value, int min, int max) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required and cannot be blank.");
        }
        String t = value.trim();
        if (t.length() < min || t.length() > max) {
            throw new ValidationException(field + " must be " + min + "-" + max
                    + " characters long (entered " + t.length() + ").");
        }
        return t;
    }

    static void requireNotNull(String field, Object value) throws ValidationException {
        if (value == null) {
            throw new ValidationException(field + " must reference an existing record.");
        }
    }

    static void requireEmail(String field, String value) throws ValidationException {
        String t = requireText(field, value, 5, MAX_EMAIL_LENGTH);
        if (!t.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")) {
            throw new ValidationException(field + " is not a valid email address (example: name@auca.ac.rw).");
        }
    }

    static void requirePhone(String field, String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required and cannot be blank.");
        }
        String t = value.trim();
        if (!t.matches("\\+?\\d+")) {
            throw new ValidationException(field + " may contain only digits with an optional leading +.");
        }
        int digits = t.startsWith("+") ? t.length() - 1 : t.length();
        if (digits < MIN_PHONE_LENGTH || digits > MAX_PHONE_LENGTH) {
            throw new ValidationException(field + " must contain " + MIN_PHONE_LENGTH + "-"
                    + MAX_PHONE_LENGTH + " digits (entered " + digits + ").");
        }
    }

    static void requireIsbn(String field, String value) throws ValidationException {
        String t = requireText(field, value, 10, 17);
        String digits = t.replace("-", "");
        if (!(digits.matches("\\d{13}") || digits.matches("\\d{9}[0-9Xx]"))) {
            throw new ValidationException(field + " must be a valid ISBN-10 or ISBN-13 (hyphens optional).");
        }
    }

    static void requirePast(String field, LocalDate date) throws ValidationException {
        if (date == null) {
            throw new ValidationException(field + " is required (format " + DATE_PATTERN + ").");
        }
        if (!date.isBefore(LocalDate.now())) {
            throw new ValidationException(field + " must be in the past.");
        }
    }

    /** Documented rule: admission, hire, join, enrollment, payment and assessment dates may not be in the future. */
    static void requireNotFuture(String field, LocalDate date) throws ValidationException {
        if (date == null) {
            throw new ValidationException(field + " is required (format " + DATE_PATTERN + ").");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(field + " cannot be in the future.");
        }
    }

    static void requirePositive(String field, BigDecimal value) throws ValidationException {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(field + " must be strictly greater than zero.");
        }
    }

    /** Allowed values for the controlled-vocabulary attributes (status, semester, type, method). */
    static List<String> allowedValues(String key) {
        return switch (key) {
            case "academicStatus" -> List.of("ACTIVE", "INACTIVE");
            case "employmentType" -> List.of("FULL_TIME", "PART_TIME", "CONTRACT");
            case "enrollmentStatus" -> List.of("ACTIVE", "COMPLETED", "DROPPED");
            case "semester" -> List.of("FALL", "SPRING", "SUMMER");
            case "assessmentType" -> List.of("QUIZ", "ASSIGNMENT", "MIDTERM", "FINAL_EXAM", "PROJECT");
            case "paymentMethod" -> List.of("CASH", "CARD", "MOBILE_MONEY", "BANK_TRANSFER");
            default -> List.of();
        };
    }

    static void requireAllowed(String field, String value, String key) throws ValidationException {
        if (value == null || !allowedValues(key).contains(value)) {
            throw new ValidationException(field + " must be one of " + allowedValues(key) + ".");
        }
    }
}
