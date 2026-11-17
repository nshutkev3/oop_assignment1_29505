package com.auca.abstracts;

import com.auca.administrator.Administrator;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import com.auca.teacher.Teacher;
import java.time.LocalDate;

/**
 * Abstract base of every human actor (Student, Teacher, Administrator). Cannot be instantiated:
 * it is incomplete (abstract getRoleDescription) and only exists to share state and behaviour.
 */
public abstract class Person implements Identifiable, Registrable {

    private String personId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    /** Constructor: initialises every attribute of this type. */
    protected Person(String personId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /** Role label supplied by each concrete subclass (runtime polymorphism). */
    public abstract String getRoleDescription();

    @Override
    public String getId() {
        return personId;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireText("Person ID", personId, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireText("First name", firstName, MIN_NAME_LENGTH, MAX_NAME_LENGTH);
        Registrable.requireText("Last name", lastName, MIN_NAME_LENGTH, MAX_NAME_LENGTH);
        Registrable.requireEmail("Email", email);
        Registrable.requirePhone("Phone", phoneNumber);
        Registrable.requirePast("Date of birth", dateOfBirth);
    }

    @Override
    public void printDetails() {
        System.out.println("---- " + getRoleDescription() + " ----");
        System.out.println("Person ID         : " + (personId));
        System.out.println("Name              : " + (getDisplayName()));
        System.out.println("Email             : " + (email));
        System.out.println("Phone             : " + (phoneNumber));
        System.out.println("Date of birth     : " + (dateOfBirth));
    }

    @Override
    public String getRegistrationSummary() {
        return getRoleDescription() + " [" + personId + "] " + getDisplayName();
    }

    @Override
    public String toString() {
        return getRegistrationSummary() + " <" + email + ">";
    }
}
