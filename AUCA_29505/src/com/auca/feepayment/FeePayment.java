package com.auca.feepayment;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A fee payment made by a student (money held as BigDecimal).
 */
public class FeePayment implements Identifiable, Registrable {

    private String paymentId;
    private Student student;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String receiptNumber;

    /** Constructor: initialises every attribute of this type. */
    public FeePayment(String paymentId, Student student, BigDecimal amount, LocalDate paymentDate, String paymentMethod, String receiptNumber) {
        this.paymentId = paymentId;
        this.student = student;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    @Override
    public String getId() {
        return paymentId;
    }

    @Override
    public String getDisplayName() {
        return receiptNumber + " - " + (amount == null ? "?" : amount.toPlainString());
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireText("Payment ID", paymentId, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireNotNull("Student", student);
        Registrable.requirePositive("Amount", amount);
        Registrable.requireNotFuture("Payment date", paymentDate);
        Registrable.requireAllowed("Payment method", paymentMethod, "paymentMethod");
        Registrable.requireText("Receipt number", receiptNumber, 3, 30);
    }

    @Override
    public void printDetails() {
        System.out.println("---- Fee payment ----");
        System.out.println("Payment ID        : " + (paymentId));
        System.out.println("Student           : " + (student == null ? "(none)" : student.getDisplayName()));
        System.out.println("Amount            : " + (amount == null ? "(none)" : amount.toPlainString()));
        System.out.println("Payment date      : " + (paymentDate));
        System.out.println("Method            : " + (paymentMethod));
        System.out.println("Receipt number    : " + (receiptNumber));
    }

    @Override
    public String getRegistrationSummary() {
        return "Payment [" + paymentId + "] " + (amount == null ? "?" : amount.toPlainString()) + " by " + (student == null ? "?" : student.getDisplayName()) + " on " + paymentDate + " via " + paymentMethod + " (receipt " + receiptNumber + ")";
    }

    @Override
    public String toString() {
        return getRegistrationSummary();
    }
}
