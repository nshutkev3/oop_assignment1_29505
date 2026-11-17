package com.auca.course;

import com.auca.department.Department;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.teacher.Teacher;

/**
 * A course offered by a department and taught by an assigned teacher.
 */
public class Course implements Identifiable, Registrable {

    private String courseId;
    private String courseCode;
    private String courseTitle;
    private int creditHours;
    private Department department;
    private Teacher assignedTeacher;

    /** Constructor: initialises every attribute of this type. */
    public Course(String courseId, String courseCode, String courseTitle, int creditHours, Department department, Teacher assignedTeacher) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.creditHours = creditHours;
        this.department = department;
        this.assignedTeacher = assignedTeacher;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Teacher getAssignedTeacher() {
        return assignedTeacher;
    }

    public void setAssignedTeacher(Teacher assignedTeacher) {
        this.assignedTeacher = assignedTeacher;
    }

    @Override
    public String getId() {
        return courseId;
    }

    @Override
    public String getDisplayName() {
        return courseCode + " - " + courseTitle;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void validate() throws ValidationException {
        Registrable.requireText("Course ID", courseId, MIN_ID_LENGTH, MAX_ID_LENGTH);
        Registrable.requireText("Course code", courseCode, 3, 15);
        Registrable.requireText("Course title", courseTitle, 2, 100);
        if (creditHours < 1 || creditHours > 6) {
            throw new ValidationException("Credit hours must be between 1 and 6.");
        }
        Registrable.requireNotNull("Department", department);
        Registrable.requireNotNull("Assigned teacher", assignedTeacher);
    }

    @Override
    public void printDetails() {
        System.out.println("---- Course ----");
        System.out.println("Course ID         : " + (courseId));
        System.out.println("Code              : " + (courseCode));
        System.out.println("Title             : " + (courseTitle));
        System.out.println("Credit hours      : " + (creditHours));
        System.out.println("Department        : " + (department == null ? "(none)" : department.getDepartmentName()));
        System.out.println("Teacher           : " + (assignedTeacher == null ? "(none)" : assignedTeacher.getDisplayName()));
    }

    @Override
    public String getRegistrationSummary() {
        return "Course [" + courseId + "] " + courseCode + " - " + courseTitle + " (" + creditHours + " credits)";
    }

    @Override
    public String toString() {
        return getRegistrationSummary();
    }
}
