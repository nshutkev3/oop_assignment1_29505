package com.auca.bookloan;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import java.time.LocalDate;

/**
 * A loan of a library book to a student. returnedDate stays null until the book is returned.
 */
public class BookLoan implements Identifiable, Registrable {

    private String loanId;
    private Student student;
    private LibraryBook book;
    private LocalDate borrowedDate;
    private LocalDate dueDate;
    private LocalDate returnedDate;

    /** Constructor: initialises every attribute of this type. */
    public BookLoan(String loanId, Student student, LibraryBook book, LocalDate borrowedDate, LocalDate dueDate, LocalDate returnedDate) {
        this.loanId = loanId;
        this.student = student;
        this.book = book;
        this.borrowedDate = borrowedDate;
        this.dueDate = dueDate;
        this.returnedDate = returnedDate;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LibraryBook getBook() {
        return book;
    }

    public void setBook(LibraryBook book) {
        this.book = book;
    }

    public LocalDate getBorrowedDate() {
        return borrowedDate;
    }

    public void setBorrowedDate(LocalDate borrowedDate) {
        this.borrowedDate = borrowedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }

    @Override
    public String getId() {
        return loanId;
    }

    @Override
    public String getDisplayName() {
        return (book == null ? "?" : book.getTitle()) + " -> " + (student == null ? "?" : student.getDisplayName());
    }

    @Override
    public boolean isActive() {
        return returnedDate == null;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireText("Loan ID", loanId, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireNotNull("Student", student);
        Registrable.requireNotNull("Book", book);
        Registrable.requireNotFuture("Borrowed date", borrowedDate);
        if (dueDate == null || !dueDate.isAfter(borrowedDate)) {
            throw new ValidationException("Due date must be after the borrowed date.");
        }
        if (returnedDate != null && returnedDate.isBefore(borrowedDate)) {
            throw new ValidationException("Returned date cannot be before the borrowed date.");
        }
    }

    @Override
    public void printDetails() {
        System.out.println("---- Book loan ----");
        System.out.println("Loan ID           : " + (loanId));
        System.out.println("Student           : " + (student == null ? "(none)" : student.getDisplayName()));
        System.out.println("Book              : " + (book == null ? "(none)" : book.getTitle()));
        System.out.println("Borrowed date     : " + (borrowedDate));
        System.out.println("Due date          : " + (dueDate));
        System.out.println("Returned date     : " + (returnedDate == null ? "(not returned yet)" : returnedDate.toString()));
    }

    @Override
    public String getRegistrationSummary() {
        return "Loan [" + loanId + "] " + getDisplayName() + " (due " + dueDate + ") " + (returnedDate == null ? "[ON LOAN]" : "[RETURNED " + returnedDate + "]");
    }

    @Override
    public String toString() {
        return getRegistrationSummary();
    }
}
