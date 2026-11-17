package com.auca.student;

import com.auca.abstracts.AcademicMember;
import com.auca.abstracts.Person;
import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Registrable;
import java.time.LocalDate;

/**
 * A student: third level of Person -> AcademicMember -> Student.
 */
public class Student extends AcademicMember {

    private String studentNumber;
    private String programName;
    private LocalDate admissionDate;
    private int studyYear;
    private String guardianPhone;
    private boolean scholarshipStatus;

    /** Constructor chaining via super(...): initialises every attribute of this type. */
    public Student(String personId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth, String memberNumber, Department department, LocalDate joinDate, String academicStatus, String institutionalEmail, String advisorName, String studentNumber, String programName, LocalDate admissionDate, int studyYear, String guardianPhone, boolean scholarshipStatus) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth, memberNumber, department, joinDate, academicStatus, institutionalEmail, advisorName);
        this.studentNumber = studentNumber;
        this.programName = programName;
        this.admissionDate = admissionDate;
        this.studyYear = studyYear;
        this.guardianPhone = guardianPhone;
        this.scholarshipStatus = scholarshipStatus;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public int getStudyYear() {
        return studyYear;
    }

    public void setStudyYear(int studyYear) {
        this.studyYear = studyYear;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public boolean isScholarshipStatus() {
        return scholarshipStatus;
    }

    public void setScholarshipStatus(boolean scholarshipStatus) {
        this.scholarshipStatus = scholarshipStatus;
    }

    @Override
    public String getRoleDescription() {
        return "Student";
    }

    @Override
    public String getAcademicCategory() {
        return scholarshipStatus ? "Scholarship student" : "Self-sponsored student";
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.requireText("Student number", studentNumber, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireText("Program name", programName, 2, 120);
        Registrable.requireNotFuture("Admission date", admissionDate);
        if (studyYear < 1 || studyYear > 6) {
            throw new ValidationException("Study year must be between 1 and 6.");
        }
        Registrable.requirePhone("Guardian phone", guardianPhone);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("Student number    : " + (studentNumber));
        System.out.println("Program           : " + (programName));
        System.out.println("Admission date    : " + (admissionDate));
        System.out.println("Study year        : " + (studyYear));
        System.out.println("Guardian phone    : " + (guardianPhone));
        System.out.println("Scholarship       : " + (scholarshipStatus ? "YES" : "NO"));
    }
}
