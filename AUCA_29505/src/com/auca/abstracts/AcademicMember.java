package com.auca.abstracts;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import com.auca.teacher.Teacher;
import java.time.LocalDate;

/**
 * Abstract middle level of the multilevel hierarchy Person -> AcademicMember -> Student/Teacher.
 */
public abstract class AcademicMember extends Person {

    private String memberNumber;
    private Department department;
    private LocalDate joinDate;
    private String academicStatus;
    private String institutionalEmail;
    private String advisorName;

    /** Constructor chaining via super(...): initialises every attribute of this type. */
    protected AcademicMember(String personId, String firstName, String lastName, String email, String phoneNumber, LocalDate dateOfBirth, String memberNumber, Department department, LocalDate joinDate, String academicStatus, String institutionalEmail, String advisorName) {
        super(personId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.memberNumber = memberNumber;
        this.department = department;
        this.joinDate = joinDate;
        this.academicStatus = academicStatus;
        this.institutionalEmail = institutionalEmail;
        this.advisorName = advisorName;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getAcademicStatus() {
        return academicStatus;
    }

    public void setAcademicStatus(String academicStatus) {
        this.academicStatus = academicStatus;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public void setInstitutionalEmail(String institutionalEmail) {
        this.institutionalEmail = institutionalEmail;
    }

    public String getAdvisorName() {
        return advisorName;
    }

    public void setAdvisorName(String advisorName) {
        this.advisorName = advisorName;
    }

    /** Category supplied by each concrete academic member (Student, Teacher). */
    public abstract String getAcademicCategory();

    @Override
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(academicStatus);
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        Registrable.requireText("Member number", memberNumber, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireNotNull("Department", department);
        Registrable.requireNotFuture("Join date", joinDate);
        Registrable.requireAllowed("Academic status", academicStatus, "academicStatus");
        Registrable.requireEmail("Institutional email", institutionalEmail);
        Registrable.requireText("Advisor name", advisorName, 2, 120);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("Member number     : " + (memberNumber));
        System.out.println("Department        : " + ((department == null ? "(none)" : department.getDepartmentName())));
        System.out.println("Join date         : " + (joinDate));
        System.out.println("Academic status   : " + (academicStatus));
        System.out.println("Institutional email: " + (institutionalEmail));
        System.out.println("Advisor           : " + (advisorName));
    }
}
