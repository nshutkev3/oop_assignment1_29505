package com.auca.enrollment;

import com.auca.course.Course;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.student.Student;
import java.time.LocalDate;

/**
 * A student's enrollment in a course for a semester.
 */
public class Enrollment implements Identifiable, Registrable {

    private String enrollmentId;
    private Student student;
    private Course course;
    private LocalDate enrollmentDate;
    private String semester;
    private String enrollmentStatus;

    /** Constructor: initialises every attribute of this type. */
    public Enrollment(String enrollmentId, Student student, Course course, LocalDate enrollmentDate, String semester, String enrollmentStatus) {
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        this.semester = semester;
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    @Override
    public String getId() {
        return enrollmentId;
    }

    @Override
    public String getDisplayName() {
        return (student == null ? "?" : student.getDisplayName()) + " -> " + (course == null ? "?" : course.getCourseCode()) + " (" + semester + ")";
    }

    @Override
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(enrollmentStatus);
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireText("Enrollment ID", enrollmentId, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireNotNull("Student", student);
        Registrable.requireNotNull("Course", course);
        Registrable.requireNotFuture("Enrollment date", enrollmentDate);
        Registrable.requireAllowed("Semester", semester, "semester");
        Registrable.requireAllowed("Enrollment status", enrollmentStatus, "enrollmentStatus");
    }

    @Override
    public void printDetails() {
        System.out.println("---- Enrollment ----");
        System.out.println("Enrollment ID     : " + (enrollmentId));
        System.out.println("Student           : " + (student == null ? "(none)" : student.getDisplayName()));
        System.out.println("Course            : " + (course == null ? "(none)" : course.getDisplayName()));
        System.out.println("Enrollment date   : " + (enrollmentDate));
        System.out.println("Semester          : " + (semester));
        System.out.println("Status            : " + (enrollmentStatus));
    }

    @Override
    public String getRegistrationSummary() {
        return "Enrollment [" + enrollmentId + "] " + getDisplayName() + " [" + enrollmentStatus + "]";
    }

    @Override
    public String toString() {
        return getRegistrationSummary();
    }
}
