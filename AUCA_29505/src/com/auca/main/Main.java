package com.auca.main;

import com.auca.abstracts.AcademicMember;
import com.auca.abstracts.Person;
import com.auca.administrator.Administrator;
import com.auca.assessment.Assessment;
import com.auca.bookloan.BookLoan;
import com.auca.classroom.Classroom;
import com.auca.course.Course;
import com.auca.department.Department;
import com.auca.enrollment.Enrollment;
import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import com.auca.teacher.Teacher;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * AUCA_ID School Management System - console application entry point.
 *
 * Design notes:
 *  - ONE shared Scanner (System.in); every read is a full nextLine() that is then parsed explicitly.
 *  - Every registration form is described by a small table of field specs (step, key, label, hint, rule)
 *    and run by ONE generic engine (workflow). This gives all 11 entities the same 4-step design:
 *    steps 1-3 collect fields, step 4 reviews / edits / saves.
 *  - Related objects are chosen from numbered lists of real saved records, never typed as raw IDs.
 *  - Records live in typed ArrayLists. Nothing is stored until the final Save passes every check.
 *  - Helper methods are private; the only public method is main (all business operations live in the interfaces).
 */
public class Main {

    private static final Scanner SC = new Scanner(System.in);

    private static final String[] ENTITIES = {"Department", "Student", "Teacher", "Administrator", "Course",
            "Classroom", "Enrollment", "Assessment", "FeePayment", "LibraryBook", "BookLoan"};
    /** Keys whose value must be unique across ALL entities' getId(). */
    private static final Set<String> ID_KEYS = Set.of("personId", "departmentId", "courseId", "classroomId",
            "enrollmentId", "assessmentId", "paymentId", "bookId", "loanId");

    private static final Object BACK = new Object();
    private static final Object CANCEL = new Object();
    private static final String RULE = "+" + "-".repeat(60) + "+";

    private static final List<Department> departments = new ArrayList<>();
    private static final List<Student> students = new ArrayList<>();
    private static final List<Teacher> teachers = new ArrayList<>();
    private static final List<Administrator> administrators = new ArrayList<>();
    private static final List<Course> courses = new ArrayList<>();
    private static final List<Classroom> classrooms = new ArrayList<>();
    private static final List<Enrollment> enrollments = new ArrayList<>();
    private static final List<Assessment> assessments = new ArrayList<>();
    private static final List<FeePayment> payments = new ArrayList<>();
    private static final List<LibraryBook> books = new ArrayList<>();
    private static final List<BookLoan> loans = new ArrayList<>();

    // =================================================================== entry point + menu

    public static void main(String[] args) {
        System.out.println("AUCA_ID School Management System  (" + Identifiable.ID_PREFIX
                + ", schema v" + Identifiable.ENTITY_VERSION + ")");
        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = Integer.parseInt(readLine("Select an option: "));
                running = dispatch(choice);
            } catch (NumberFormatException e) {
                System.out.println("! Invalid selection: please type a number from the menu (0-19).");
            } catch (ValidationException | DuplicateRecordException | RecordNotFoundException
                     | PrerequisiteMissingException | BookUnavailableException e) {
                System.out.println("! " + e.getMessage());
                System.out.println("  Returning to the main menu. Nothing partial was saved.");
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date. Use YYYY-MM-DD (example: 2004-05-16). Returning to the main menu.");
            } catch (NoSuchElementException e) {
                System.out.println("\nInput closed. Goodbye.");
                running = false;
            } catch (RuntimeException e) {
                System.out.println("! Unexpected problem: " + e.getMessage() + ". Returning to the main menu.");
            }
        }
    }

    private static void printMenu() {
        String[] left = {"Register Department", "Register Student", "Register Teacher", "Register Administrator",
                "Register Course", "Register Classroom", "Enroll Student", "Record Assessment",
                "Record Fee Payment", "Register Library Book"};
        String[] right = {"Borrow Library Book", "Return Library Book", "View All Records", "Search Record by ID",
                "Update Existing Record", "Student Academic Summary", "Student Payment History",
                "View Available Books", "Demonstrate Polymorphism", "Exit"};
        System.out.println("\n============== AUCA_ID SCHOOL MANAGEMENT ==============");
        for (int i = 0; i < 10; i++) {
            System.out.printf(" %2d. %-26s %2d. %s%n", i + 1, left[i], i == 9 ? 0 : i + 11, right[i]);
        }
    }

    private static boolean dispatch(int choice) throws ValidationException {
        switch (choice) {
            case 1 -> registerFromMenu("Department");
            case 2 -> registerFromMenu("Student");
            case 3 -> registerFromMenu("Teacher");
            case 4 -> registerFromMenu("Administrator");
            case 5 -> registerFromMenu("Course");
            case 6 -> registerFromMenu("Classroom");
            case 7 -> registerFromMenu("Enrollment");
            case 8 -> registerFromMenu("Assessment");
            case 9 -> registerFromMenu("FeePayment");
            case 10 -> registerFromMenu("LibraryBook");
            case 11 -> registerFromMenu("BookLoan");
            case 12 -> returnBook();
            case 13 -> viewAll();
            case 14 -> searchById();
            case 15 -> updateRecord();
            case 16 -> studentSummary();
            case 17 -> paymentHistory();
            case 18 -> availableBooks();
            case 19 -> demonstratePolymorphism();
            case 0 -> {
                System.out.println("Goodbye!");
                return false;
            }
            default -> System.out.println("! Option " + choice + " is out of range. Choose 0-19.");
        }
        return true;
    }

    // =================================================================== basic input helpers

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return SC.nextLine().trim();
    }

    private static int askInt(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("! Please enter a whole number.");
            }
        }
    }

    private static boolean confirm(String prompt) {
        while (true) {
            String s = readLine(prompt);
            if (s.equalsIgnoreCase("YES") || s.equalsIgnoreCase("Y")) {
                return true;
            }
            if (s.equalsIgnoreCase("NO") || s.equalsIgnoreCase("N")) {
                return false;
            }
            System.out.println("! Please answer YES or NO.");
        }
    }

    private static String label(String entity) {
        return entity.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    private static String article(String word) {
        return "AEIOU".indexOf(Character.toUpperCase(word.charAt(0))) >= 0 ? "an " : "a ";
    }

    private static void boxLine(String s) {
        System.out.println("| " + String.format("%-58s", s) + " |");
    }

    private static void boxLine(String l, String r) {
        boxLine(l + " ".repeat(Math.max(1, 58 - l.length() - r.length())) + r);
    }

    // =================================================================== data access helpers

    private static List<? extends Identifiable> allOf(String type) {
        return switch (type) {
            case "Department" -> departments;
            case "Student" -> students;
            case "Teacher" -> teachers;
            case "Administrator" -> administrators;
            case "Course" -> courses;
            case "Classroom" -> classrooms;
            case "Enrollment" -> enrollments;
            case "Assessment" -> assessments;
            case "FeePayment" -> payments;
            case "LibraryBook" -> books;
            case "BookLoan" -> loans;
            default -> throw new IllegalArgumentException("Unknown entity type " + type);
        };
    }

    /** Records that may be chosen as a reference: only ACTIVE enrollments, only books with stock. */
    private static List<? extends Identifiable> candidates(String type) {
        return switch (type) {
            case "Enrollment" -> enrollments.stream().filter(Enrollment::isActive).toList();
            case "LibraryBook" -> books.stream().filter(b -> b.getAvailableCopies() > 0).toList();
            default -> allOf(type);
        };
    }

    private static List<Identifiable> allEntities() {
        List<Identifiable> all = new ArrayList<>();
        for (String t : ENTITIES) {
            all.addAll(allOf(t));
        }
        return all;
    }

    private static String summary(Identifiable i) {
        return ((Registrable) i).getRegistrationSummary();
    }

    private static Object getter(Object o, String key) {
        String cap = Character.toUpperCase(key.charAt(0)) + key.substring(1);
        for (Method m : o.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && (m.getName().equals("get" + cap) || m.getName().equals("is" + cap))) {
                try {
                    return m.invoke(o);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot read " + key, e);
                }
            }
        }
        return null;
    }

    private static void setter(Object o, String key, Object value) {
        String name = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
        for (Method m : o.getClass().getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                try {
                    m.invoke(o, value);
                    return;
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Cannot update " + key, e);
                }
            }
        }
        throw new IllegalStateException("No setter for " + key);
    }



    private static String[] row(int step, String key, String label, String hint, String rule) {
        return new String[]{String.valueOf(step), key, label, hint, rule};
    }

    private static void personRows(List<String[]> l) {
        l.add(row(1, "personId", "Person ID", "String | unique | 4-30 chars", "u:text,4,30"));
        l.add(row(1, "firstName", "First name", "String | 2-60 chars", "text,2,60"));
        l.add(row(1, "lastName", "Last name", "String | 2-60 chars", "text,2,60"));
        l.add(row(1, "email", "Email", "String | unique | email", "u:email"));
        l.add(row(1, "phoneNumber", "Phone", "String | 10-15 digits, optional +", "phone"));
        l.add(row(1, "dateOfBirth", "Date of birth", "LocalDate | YYYY-MM-DD", "date,past"));
    }

    private static void memberRows(List<String[]> l) {
        l.add(row(2, "memberNumber", "Member number", "String | unique | 4-30", "u:text,4,30"));
        l.add(row(2, "department", "Department", "Department | select", "ref,Department"));
        l.add(row(2, "joinDate", "Join date", "LocalDate | YYYY-MM-DD", "date,notfuture"));
        l.add(row(2, "academicStatus", "Academic status", "", "enum,academicStatus"));
        l.add(row(2, "institutionalEmail", "Institutional email", "String | unique | email", "u:email"));
        l.add(row(2, "advisorName", "Advisor name", "String | 2-120 chars", "text,2,120"));
    }

    /**
     * Field table: {step, key, label, hint, rule}. Rule grammar: "u:" = unique, "!" = locked on update,
     * text,min,max | email | phone | isbn | date,past|notfuture|after:key | int,min,max|@key | dbl,pos | dbl,min,max|@key
     * | money | enum,key | bool | ref,Type | refopt,Type | derived.
     */
    private static String[][] fields(String e) {
        List<String[]> l = new ArrayList<>();
        switch (e) {
            case "Department" -> {
                l.add(row(1, "departmentId", "Department ID", "String | unique | 4-30 chars", "u:text,4,30"));
                l.add(row(1, "departmentName", "Department name", "String | unique | 2-60 chars", "u:text,2,60"));
                l.add(row(2, "departmentCode", "Department code", "String | unique | 2-10 chars", "u:text,2,10"));
                l.add(row(2, "officeLocation", "Office location", "String | 2-60 chars", "text,2,60"));
                l.add(row(3, "contactEmail", "Contact email", "String | email", "email"));
                l.add(row(3, "headTeacher", "Head teacher", "Teacher | optional | select", "refopt,Teacher"));
            }
            case "Student" -> {
                personRows(l);
                memberRows(l);
                l.add(row(3, "studentNumber", "Student number", "String | unique | 4-30", "u:text,4,30"));
                l.add(row(3, "programName", "Program name", "String | 2-120 chars", "text,2,120"));
                l.add(row(3, "admissionDate", "Admission date", "LocalDate | YYYY-MM-DD", "date,notfuture"));
                l.add(row(3, "studyYear", "Study year", "int | 1-6", "int,1,6"));
                l.add(row(3, "guardianPhone", "Guardian phone", "String | 10-15 digits", "phone"));
                l.add(row(3, "scholarshipStatus", "Scholarship", "", "bool"));
            }
            case "Teacher" -> {
                personRows(l);
                memberRows(l);
                l.add(row(3, "employeeNumber", "Employee number", "String | unique | 4-30", "u:text,4,30"));
                l.add(row(3, "specialization", "Specialization", "String | 2-100 chars", "text,2,100"));
                l.add(row(3, "hireDate", "Hire date", "LocalDate | YYYY-MM-DD", "date,notfuture"));
                l.add(row(3, "salary", "Salary", "BigDecimal | > 0, max 2 decimals", "money"));
                l.add(row(3, "officeNumber", "Office number", "String | 1-20 chars", "text,1,20"));
                l.add(row(3, "employmentType", "Employment type", "", "enum,employmentType"));
            }
            case "Administrator" -> {
                personRows(l);
                l.add(row(2, "staffNumber", "Staff number", "String | unique | 4-30", "u:text,4,30"));
                l.add(row(2, "jobTitle", "Job title", "String | 2-60 chars", "text,2,60"));
                l.add(row(2, "assignedOffice", "Assigned office", "String | 2-60 chars", "text,2,60"));
                l.add(row(3, "employmentDate", "Employment date", "LocalDate | YYYY-MM-DD", "date,notfuture"));
                l.add(row(3, "accessLevel", "Access level", "int | 1-5", "int,1,5"));
                l.add(row(3, "active", "Active", "", "bool"));
            }
            case "Course" -> {
                l.add(row(1, "courseId", "Course ID", "String | unique | 4-30 chars", "u:text,4,30"));
                l.add(row(1, "courseCode", "Course code", "String | unique | 3-15 chars", "u:text,3,15"));
                l.add(row(2, "courseTitle", "Course title", "String | 2-100 chars", "text,2,100"));
                l.add(row(2, "creditHours", "Credit hours", "int | 1-6", "int,1,6"));
                l.add(row(3, "department", "Department", "Department | select", "ref,Department"));
                l.add(row(3, "assignedTeacher", "Assigned teacher", "Teacher | select", "ref,Teacher"));
            }
            case "Classroom" -> {
                l.add(row(1, "classroomId", "Classroom ID", "String | unique | 4-30 chars", "u:text,4,30"));
                l.add(row(1, "buildingName", "Building name", "String | 2-60 chars", "text,2,60"));
                l.add(row(2, "roomNumber", "Room number", "String | 1-10 chars", "text,1,10"));
                l.add(row(2, "capacity", "Capacity", "int | > 0", "int,1,5000"));
                l.add(row(3, "floorNumber", "Floor number", "int | >= 0", "int,0,200"));
                l.add(row(3, "available", "Available", "", "bool"));
            }
            case "Enrollment" -> {
                l.add(row(1, "enrollmentId", "Enrollment ID", "String | unique | 4-30 chars", "u:text,4,30"));
                l.add(row(1, "student", "Student", "Student | select", "ref,Student"));
                l.add(row(2, "course", "Course", "Course | select", "ref,Course"));
                l.add(row(2, "enrollmentDate", "Enrollment date", "LocalDate | YYYY-MM-DD", "date,notfuture"));
                l.add(row(3, "semester", "Semester", "", "enum,semester"));
                l.add(row(3, "enrollmentStatus", "Enrollment status", "", "enum,enrollmentStatus"));
            }
            case "Assessment" -> {
                l.add(row(1, "assessmentId", "Assessment ID", "String | unique | 4-30 chars", "u:text,4,30"));
                l.add(row(1, "enrollment", "Enrollment", "active Enrollment | select", "ref,Enrollment"));
                l.add(row(2, "assessmentType", "Assessment type", "", "enum,assessmentType"));
                l.add(row(2, "maximumScore", "Maximum score", "double | > 0", "dbl,pos"));
                l.add(row(3, "obtainedScore", "Obtained score", "double | 0..maximum score", "dbl,0,@maximumScore"));
                l.add(row(3, "assessmentDate", "Assessment date", "LocalDate | YYYY-MM-DD", "date,notfuture"));
            }
            case "FeePayment" -> {
                l.add(row(1, "paymentId", "Payment ID", "String | unique | 4-30 chars", "u:text,4,30"));
                l.add(row(1, "student", "Student", "Student | select", "ref,Student"));
                l.add(row(2, "amount", "Amount", "BigDecimal | > 0, max 2 decimals", "money"));
                l.add(row(2, "paymentDate", "Payment date", "LocalDate | YYYY-MM-DD", "date,notfuture"));
                l.add(row(3, "paymentMethod", "Payment method", "", "enum,paymentMethod"));
                l.add(row(3, "receiptNumber", "Receipt number", "String | unique | 3-30 chars", "u:text,3,30"));
            }
            case "LibraryBook" -> {
                l.add(row(1, "bookId", "Book ID", "String | unique | 4-30 chars", "u:text,4,30"));
                l.add(row(1, "isbn", "ISBN", "String | unique | ISBN-10/13", "u:isbn"));
                l.add(row(2, "title", "Title", "String | 2-120 chars", "text,2,120"));
                l.add(row(2, "author", "Author", "String | 2-60 chars", "text,2,60"));
                l.add(row(3, "totalCopies", "Total copies", "int | >= 0", "int,0,10000"));
                l.add(row(3, "availableCopies", "Available copies", "int | 0..total copies", "int,0,@totalCopies"));
            }
            case "BookLoan" -> {
                l.add(row(1, "loanId", "Loan ID", "String | unique | 4-30 chars", "!u:text,4,30"));
                l.add(row(1, "student", "Student", "Student | select", "!ref,Student"));
                l.add(row(2, "book", "Library book", "LibraryBook | select (in stock)", "!ref,LibraryBook"));
                l.add(row(2, "borrowedDate", "Borrowed date", "LocalDate | YYYY-MM-DD", "!date,notfuture"));
                l.add(row(3, "dueDate", "Due date", "LocalDate | after borrowed date", "date,after:borrowedDate"));
                l.add(row(3, "returnedDate", "Returned date", "derived: empty until returned", "derived"));
            }
            default -> throw new IllegalArgumentException("Unknown entity " + e);
        }
        return l.toArray(new String[0][]);
    }

    private static String[] stepTitles(String e) {
        return switch (e) {
            case "Student" -> new String[]{"PERSONAL DETAILS", "ACADEMIC DETAILS", "STUDENT DETAILS"};
            case "Teacher" -> new String[]{"PERSONAL DETAILS", "ACADEMIC DETAILS", "TEACHER DETAILS"};
            case "Administrator" -> new String[]{"PERSONAL DETAILS", "STAFF DETAILS", "ACCESS DETAILS"};
            case "Department" -> new String[]{"IDENTITY", "CONTACT & LOCATION", "LEADERSHIP"};
            case "Course" -> new String[]{"IDENTITY", "COURSE DETAILS", "ASSIGNMENTS"};
            case "Classroom" -> new String[]{"IDENTITY", "ROOM DETAILS", "AVAILABILITY"};
            case "Enrollment" -> new String[]{"IDENTITY & STUDENT", "COURSE & DATE", "TERM & STATUS"};
            case "Assessment" -> new String[]{"IDENTITY & ENROLLMENT", "TYPE & MAXIMUM", "SCORE & DATE"};
            case "FeePayment" -> new String[]{"IDENTITY & STUDENT", "AMOUNT & DATE", "METHOD & RECEIPT"};
            case "LibraryBook" -> new String[]{"IDENTITY", "BOOK DETAILS", "COPIES"};
            default -> new String[]{"LOAN & STUDENT", "BOOK & BORROW DATE", "DUE & RETURN"};
        };
    }

    private static String ruleOf(String[] f) {
        return f[4].replace("!", "");
    }

    private static boolean locked(String[] f) {
        return f[4].startsWith("!") || ruleOf(f).equals("derived");
    }

    private static String hintOf(String[] f) {
        String r = ruleOf(f);
        if (r.startsWith("enum,")) {
            List<String> a = Registrable.allowedValues(r.substring(5));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < a.size(); i++) {
                sb.append(i == 0 ? "" : " / ").append(i + 1).append(' ').append(a.get(i));
            }
            return sb.toString();
        }
        if (r.equals("bool")) {
            return "1 YES / 2 NO";
        }
        return f[3];
    }

    private static String fmt(Object v) {
        if (v == null) {
            return "(none)";
        }
        if (v instanceof Identifiable i) {
            return "[" + i.getId() + "] " + i.getDisplayName();
        }
        if (v instanceof Boolean b) {
            return b ? "YES" : "NO";
        }
        if (v instanceof BigDecimal d) {
            return d.toPlainString();
        }
        return v.toString();
    }


    private static void registerFromMenu(String entity) {
        while (true) {
            Object saved = workflow(entity, null);
            if (saved == null) {
                return;
            }
            System.out.println(" [1] Register another   [2] View " + label(entity).toLowerCase() + "   [0] Main menu");
            int c = askInt("Select option: ");
            if (c == 1) {
                continue;
            }
            if (c == 2) {
                ((Registrable) saved).printDetails();
            }
            return;
        }
    }


    private static Object workflow(String entity, Object existing) {
        String[][] fields = fields(entity);
        String[] stepTitles = stepTitles(entity);
        boolean update = existing != null;
        Map<String, Object> vals = new LinkedHashMap<>();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        if (update) {
            for (String[] f : fields) {
                vals.put(f[1], getter(existing, f[1]));
            }
            snapshot.putAll(vals);
        }
        int step = update ? 4 : 1;
        boolean bannerShown = false;
        while (true) {
            if (step <= 3) {
                if (!bannerShown) {
                    banner(entity, stepTitles, update);
                    bannerShown = true;
                }
                System.out.println("\n[STEP " + step + "/4] " + stepTitles[step - 1]);
                System.out.println("[ENTER] keeps shown value   [BACK] previous step   [CANCEL] exit");
                boolean back = false;
                for (int i = 0; i < fields.length && !back; i++) {
                    if (!fields[i][0].equals(String.valueOf(step)) || ruleOf(fields[i]).equals("derived")) {
                        continue;
                    }
                    Object v = askField(entity, i, fields, vals, existing);
                    if (v == CANCEL) {
                        System.out.println("- " + label(entity) + " form cancelled. Nothing was saved.");
                        return null;
                    }
                    if (v == BACK) {
                        back = true;
                    } else {
                        vals.put(fields[i][1], v);
                    }
                }
                if (back) {
                    if (step == 1) {
                        System.out.println("(already at the first step)");
                    }
                    step = Math.max(1, step - 1);
                } else {
                    step++;
                }
                continue;
            }

            printReview(entity, fields, stepTitles, vals, update);
            int choice = askInt("Select option: ");
            if (choice == 1) {
                try {
                    if (update) {
                        if (!confirm("Apply these changes to " + ((Identifiable) existing).getId() + "? (YES/NO): ")) {
                            System.out.println("Update not applied.");
                            continue;
                        }
                        saveUpdate(entity, existing, fields, vals, snapshot);
                        System.out.println("+ " + label(entity) + " " + ((Identifiable) existing).getId()
                                + " updated successfully.");
                        return existing;
                    }
                    Object saved = save(entity, fields, vals);
                    System.out.println("+ " + label(entity) + " " + ((Identifiable) saved).getId()
                            + " registered successfully.");
                    return saved;
                } catch (ValidationException | DuplicateRecordException | RecordNotFoundException
                         | BookUnavailableException e) {
                    System.out.println("! Cannot save: " + e.getMessage());
                    System.out.println("  Use [2] Edit field to correct it, or [0] Cancel.");
                }
            } else if (choice == 2) {
                int n = askInt("Field number to edit (1-" + fields.length + ", 0 = back to review): ");
                if (n == 0) {
                    continue;
                }
                if (n < 1 || n > fields.length) {
                    System.out.println("! No such field. Choose 01-" + String.format("%02d", fields.length) + ".");
                    continue;
                }
                String[] f = fields[n - 1];
                if (ruleOf(f).equals("derived") || (update && locked(f))) {
                    System.out.println("! " + f[2] + " cannot be edited.");
                    continue;
                }
                Object v = askField(entity, n - 1, fields, vals, existing);
                if (v == CANCEL || v == BACK) {
                    System.out.println("Edit abandoned; the old value is kept.");
                } else {
                    vals.put(f[1], v);
                }
            } else if (choice == 0) {
                if (confirm(update ? "Discard these edits? (YES/NO): "
                        : "Discard this unsaved " + label(entity).toLowerCase() + "? (YES/NO): ")) {
                    System.out.println("- " + label(entity) + " form cancelled. Nothing was saved.");
                    return null;
                }
            } else {
                System.out.println("! Choose 1, 2 or 0.");
            }
        }
    }

    private static String formTitle(String entity, boolean update) {
        if (entity.equals("BookLoan")) {
            return update ? "LOAN UPDATE" : "BORROW LIBRARY BOOK";
        }
        return label(entity).toUpperCase() + (update ? " UPDATE" : " REGISTRATION");
    }

    private static void banner(String entity, String[] st, boolean update) {
        System.out.println();
        System.out.println(RULE);
        boxLine("AUCA_ID / SCHOOL MANAGEMENT");
        boxLine(formTitle(entity, update), "4 steps");
        System.out.println(RULE);
        boxLine("[1] " + st[0]);
        boxLine("[2] " + st[1]);
        boxLine("[3] " + st[2]);
        boxLine("[4] Review & confirm");
        System.out.println(RULE);
        boxLine("Enter the requested value. Type BACK to return;");
        boxLine("CANCEL to discard the unsaved " + label(entity).toLowerCase() + ".");
        System.out.println(RULE);
    }

    private static void printReview(String entity, String[][] fields, String[] st, Map<String, Object> vals,
                                    boolean update) {
        System.out.println();
        System.out.println(RULE);
        boxLine("REVIEW " + label(entity).toUpperCase() + (update ? " (UPDATE)" : ""), "Step 4 of 4");
        System.out.println(RULE);
        String lastStep = "";
        for (int i = 0; i < fields.length; i++) {
            String[] f = fields[i];
            if (!f[0].equals(lastStep)) {
                boxLine(st[Integer.parseInt(f[0]) - 1]);
                lastStep = f[0];
            }
            Object v = vals.get(f[1]);
            String shown = v == null && ruleOf(f).equals("derived") ? "(not returned yet)" : fmt(v);
            String tag = update && locked(f) ? "  [locked]" : "";
            boxLine(String.format("%02d %-20s: %s%s", i + 1, f[2], shown, tag));
        }
        System.out.println(RULE);
        boxLine(update ? "[1] Save changes   [2] Edit field   [0] Cancel"
                : "[1] Save " + label(entity).toLowerCase() + "   [2] Edit field   [0] Cancel");
        System.out.println(RULE);
    }

    /** Asks ONE field (or opens the selector for a reference). Returns the value, BACK or CANCEL. */
    private static Object askField(String entity, int idx, String[][] fields, Map<String, Object> vals, Object self) {
        String[] f = fields[idx];
        if (ruleOf(f).startsWith("ref")) {
            return selectRef(entity, idx, fields, vals);
        }
        Object cur = vals.get(f[1]);
        String curText = cur != null ? "  (current: " + fmt(cur) + ")" : "";
        String num = String.format("%02d", idx + 1);
        while (true) {
            String line = readLine(String.format("%s %-18s [%s]%s : ", num, f[2], hintOf(f), curText));
            if (line.equalsIgnoreCase("CANCEL")) {
                return CANCEL;
            }
            if (line.equalsIgnoreCase("BACK")) {
                return BACK;
            }
            if (line.isEmpty() && cur != null) {
                return cur;
            }
            try {
                return parseField(f, line, vals, self);
            } catch (ValidationException | DuplicateRecordException e) {
                System.out.println("! " + e.getMessage());
                System.out.println("  Please re-enter field " + num + ":");
            }
        }
    }


    private static Object parseField(String[] f, String in, Map<String, Object> vals, Object self)
            throws ValidationException {
        String rule = ruleOf(f);
        boolean unique = rule.startsWith("u:");
        if (unique) {
            rule = rule.substring(2);
        }
        String[] p = rule.split(",");
        String lbl = f[2];
        Object out = null;
        switch (p[0]) {
            case "text" -> out = Registrable.requireText(lbl, in, Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            case "email" -> {
                Registrable.requireEmail(lbl, in);
                out = in;
            }
            case "phone" -> {
                Registrable.requirePhone(lbl, in);
                out = in;
            }
            case "isbn" -> {
                Registrable.requireIsbn(lbl, in);
                out = in;
            }
            case "date" -> out = parseDate(lbl, in, p[1], vals);
            case "int" -> out = parseInt(lbl, in, p[1], p[2], vals);
            case "dbl" -> out = parseDouble(lbl, in, p, vals);
            case "money" -> out = parseMoney(lbl, in);
            case "enum" -> out = parseEnum(lbl, in, p[1]);
            case "bool" -> out = parseBool(lbl, in);
            default -> throw new ValidationException("No input rule defined for " + lbl + ".");
        }
        if (unique) {
            checkUnique(f[1], lbl, out, self);
        }
        return out;
    }

    private static LocalDate parseDate(String lbl, String in, String mode, Map<String, Object> vals)
            throws ValidationException {
        LocalDate d;
        try {
            d = LocalDate.parse(in);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date: use YYYY-MM-DD (example: 2004-05-16).");
        }
        if (mode.equals("past")) {
            Registrable.requirePast(lbl, d);
        } else if (mode.equals("notfuture")) {
            Registrable.requireNotFuture(lbl, d);
        } else if (mode.startsWith("after:")) {
            Object other = vals.get(mode.substring(6));
            if (other instanceof LocalDate o && !d.isAfter(o)) {
                throw new ValidationException(lbl + " must be after " + o + ".");
            }
        }
        return d;
    }

    private static Integer parseInt(String lbl, String in, String lo, String hi, Map<String, Object> vals)
            throws ValidationException {
        int v;
        try {
            v = Integer.parseInt(in);
        } catch (NumberFormatException e) {
            throw new ValidationException(lbl + " must be a whole number (entered '" + in + "').");
        }
        int min = Integer.parseInt(lo);
        int max = hi.startsWith("@") && vals.get(hi.substring(1)) instanceof Integer m ? m
                : hi.startsWith("@") ? Integer.MAX_VALUE : Integer.parseInt(hi);
        if (v < min || v > max) {
            throw new ValidationException(lbl + " must be between " + min + " and " + max + ".");
        }
        return v;
    }

    private static Double parseDouble(String lbl, String in, String[] p, Map<String, Object> vals)
            throws ValidationException {
        double v;
        try {
            v = Double.parseDouble(in);
        } catch (NumberFormatException e) {
            throw new ValidationException(lbl + " must be a number (entered '" + in + "').");
        }
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            throw new ValidationException(lbl + " must be a finite number.");
        }
        if (p[1].equals("pos")) {
            if (v <= 0) {
                throw new ValidationException(lbl + " must be greater than zero.");
            }
        } else {
            double min = Double.parseDouble(p[1]);
            double max = p[2].startsWith("@") && vals.get(p[2].substring(1)) instanceof Double m ? m
                    : Double.MAX_VALUE;
            if (v < min || v > max) {
                throw new ValidationException(lbl + " must be between " + min + " and "
                        + (max == Double.MAX_VALUE ? "the maximum" : String.valueOf(max)) + ".");
            }
        }
        return v;
    }

    private static BigDecimal parseMoney(String lbl, String in) throws ValidationException {
        BigDecimal b;
        try {
            b = new BigDecimal(in);
        } catch (NumberFormatException e) {
            throw new ValidationException(lbl + " must be a decimal amount such as 1500.50 (entered '" + in + "').");
        }
        Registrable.requirePositive(lbl, b);
        if (b.scale() > 2) {
            throw new ValidationException(lbl + " may have at most 2 decimal places.");
        }
        return b.setScale(2);
    }

    private static String parseEnum(String lbl, String in, String key) throws ValidationException {
        List<String> allowed = Registrable.allowedValues(key);
        if (in.matches("\\d{1,3}")) {
            int n = Integer.parseInt(in);
            if (n >= 1 && n <= allowed.size()) {
                return allowed.get(n - 1);
            }
        }
        String up = in.toUpperCase().replace(' ', '_');
        if (allowed.contains(up)) {
            return up;
        }
        throw new ValidationException(lbl + " must be one of: " + hintOfAllowed(allowed) + ".");
    }

    private static String hintOfAllowed(List<String> a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.size(); i++) {
            sb.append(i == 0 ? "" : ", ").append(i + 1).append('=').append(a.get(i));
        }
        return sb.toString();
    }

    private static Boolean parseBool(String lbl, String in) throws ValidationException {
        String s = in.toUpperCase();
        if (s.equals("1") || s.equals("YES") || s.equals("Y") || s.equals("TRUE")) {
            return Boolean.TRUE;
        }
        if (s.equals("2") || s.equals("NO") || s.equals("N") || s.equals("FALSE")) {
            return Boolean.FALSE;
        }
        throw new ValidationException(lbl + " must be 1 (YES) or 2 (NO).");
    }

    private static void checkUnique(String key, String lbl, Object value, Object self) {
        String v = String.valueOf(value);
        for (Identifiable e : allEntities()) {
            if (e == self) {
                continue;
            }
            boolean clash = ID_KEYS.contains(key) && e.getId().equalsIgnoreCase(v);
            if (!clash) {
                Object other = getter(e, key);
                clash = other != null && other.toString().equalsIgnoreCase(v);
            }
            if (clash) {
                throw new DuplicateRecordException(lbl + " " + v + " already exists.");
            }
        }
    }


    private static Object selectRef(String entity, int idx, String[][] fields, Map<String, Object> vals) {
        String[] f = fields[idx];
        String rule = ruleOf(f);
        boolean optional = rule.startsWith("refopt");
        String type = rule.split(",")[1];
        Object cur = vals.get(f[1]);
        String num = String.format("%02d", idx + 1);
        while (true) {
            List<? extends Identifiable> list = candidates(type);
            if (list.isEmpty()) {
                if (optional) {
                    System.out.println(num + " " + f[2] + ": no " + label(type)
                            + " is registered yet - you can assign one later with Update Record.");
                    return null;
                }
                if (type.equals("LibraryBook") && !books.isEmpty()) {
                    throw new BookUnavailableException("All library books are currently out of stock. "
                            + "A copy must be returned (or new copies added) before borrowing.");
                }
                offerPrerequisite(type, entity);
                continue;
            }
            System.out.println();
            System.out.println("+----------------- CHOOSE " + label(type).toUpperCase() + " -----------------+");
            for (int i = 0; i < list.size(); i++) {
                System.out.println(" " + (i + 1) + " [" + list.get(i).getId() + "] " + list.get(i).getDisplayName());
            }
            System.out.println(" " + (list.size() + 1) + " [+] Register a new " + label(type));
            System.out.println(optional ? " 0 No " + label(type).toLowerCase() + " (assign later)"
                    : " 0 Cancel " + label(entity).toLowerCase() + " form");
            if (cur != null) {
                System.out.println("   (current: " + fmt(cur) + " - press ENTER to keep)");
            }
            String line = readLine(num + " " + f[2] + " - select option: ");
            if (line.equalsIgnoreCase("CANCEL")) {
                return CANCEL;
            }
            if (line.equalsIgnoreCase("BACK")) {
                return BACK;
            }
            if (line.isEmpty() && cur != null) {
                return cur;
            }
            int c;
            try {
                c = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("! Please type the number of an option.");
                continue;
            }
            if (c == 0) {
                return optional ? null : CANCEL;
            }
            if (c == list.size() + 1) {
                runNested(type);
                System.out.println("Returning to the " + label(entity).toLowerCase() + " form (entered values are kept)...");
                continue;
            }
            if (c >= 1 && c <= list.size()) {
                return list.get(c - 1);
            }
            System.out.println("! Invalid choice; pick a number from the list.");
        }
    }

    private static void runNested(String type) {
        try {
            workflow(type, null);
        } catch (PrerequisiteMissingException | BookUnavailableException e) {
            System.out.println("! " + e.getMessage());
        }
    }

    private static String purpose(String entity) {
        return switch (entity) {
            case "Enrollment" -> "enrolling a student";
            case "Assessment" -> "recording an assessment";
            case "FeePayment" -> "recording a fee payment";
            case "BookLoan" -> "borrowing a book";
            default -> "registering " + article(label(entity)) + label(entity).toLowerCase();
        };
    }

    private static void offerPrerequisite(String type, String entity) {
        String shown = (type.equals("Enrollment") ? "active " : "") + label(type);
        String msg = "No " + shown + " exists. Register " + article(label(type)) + label(type)
                + " before " + purpose(entity) + ".";
        System.out.println("\n! " + msg);
        System.out.println("  1 Register " + label(type) + " now");
        System.out.println("  2 Return to menu (cancel the " + label(entity).toLowerCase() + ")");
        while (true) {
            int c = askInt("Select: ");
            if (c == 1) {
                runNested(type);
                return;
            }
            if (c == 2 || c == 0) {
                throw new PrerequisiteMissingException(msg + " The " + label(entity).toLowerCase()
                        + " was not saved.");
            }
            System.out.println("! Choose 1 or 2.");
        }
    }

    // =================================================================== saving, updating, cross-record rules

    private static String str(Map<String, Object> v, String k) {
        return (String) v.get(k);
    }

    private static LocalDate dt(Map<String, Object> v, String k) {
        return (LocalDate) v.get(k);
    }

    private static Object build(String e, Map<String, Object> v) {
        return switch (e) {
            case "Department" -> new Department(str(v, "departmentId"), str(v, "departmentName"),
                    str(v, "departmentCode"), str(v, "officeLocation"), str(v, "contactEmail"),
                    (Teacher) v.get("headTeacher"));
            case "Student" -> new Student(str(v, "personId"), str(v, "firstName"), str(v, "lastName"),
                    str(v, "email"), str(v, "phoneNumber"), dt(v, "dateOfBirth"), str(v, "memberNumber"),
                    (Department) v.get("department"), dt(v, "joinDate"), str(v, "academicStatus"),
                    str(v, "institutionalEmail"), str(v, "advisorName"), str(v, "studentNumber"),
                    str(v, "programName"), dt(v, "admissionDate"), (Integer) v.get("studyYear"),
                    str(v, "guardianPhone"), (Boolean) v.get("scholarshipStatus"));
            case "Teacher" -> new Teacher(str(v, "personId"), str(v, "firstName"), str(v, "lastName"),
                    str(v, "email"), str(v, "phoneNumber"), dt(v, "dateOfBirth"), str(v, "memberNumber"),
                    (Department) v.get("department"), dt(v, "joinDate"), str(v, "academicStatus"),
                    str(v, "institutionalEmail"), str(v, "advisorName"), str(v, "employeeNumber"),
                    str(v, "specialization"), dt(v, "hireDate"), (BigDecimal) v.get("salary"),
                    str(v, "officeNumber"), str(v, "employmentType"));
            case "Administrator" -> new Administrator(str(v, "personId"), str(v, "firstName"),
                    str(v, "lastName"), str(v, "email"), str(v, "phoneNumber"), dt(v, "dateOfBirth"),
                    str(v, "staffNumber"), str(v, "jobTitle"), str(v, "assignedOffice"),
                    dt(v, "employmentDate"), (Integer) v.get("accessLevel"), (Boolean) v.get("active"));
            case "Course" -> new Course(str(v, "courseId"), str(v, "courseCode"), str(v, "courseTitle"),
                    (Integer) v.get("creditHours"), (Department) v.get("department"),
                    (Teacher) v.get("assignedTeacher"));
            case "Classroom" -> new Classroom(str(v, "classroomId"), str(v, "buildingName"),
                    str(v, "roomNumber"), (Integer) v.get("capacity"), (Integer) v.get("floorNumber"),
                    (Boolean) v.get("available"));
            case "Enrollment" -> new Enrollment(str(v, "enrollmentId"), (Student) v.get("student"),
                    (Course) v.get("course"), dt(v, "enrollmentDate"), str(v, "semester"),
                    str(v, "enrollmentStatus"));
            case "Assessment" -> new Assessment(str(v, "assessmentId"), (Enrollment) v.get("enrollment"),
                    str(v, "assessmentType"), (Double) v.get("maximumScore"), (Double) v.get("obtainedScore"),
                    dt(v, "assessmentDate"));
            case "FeePayment" -> new FeePayment(str(v, "paymentId"), (Student) v.get("student"),
                    (BigDecimal) v.get("amount"), dt(v, "paymentDate"), str(v, "paymentMethod"),
                    str(v, "receiptNumber"));
            case "LibraryBook" -> new LibraryBook(str(v, "bookId"), str(v, "isbn"), str(v, "title"),
                    str(v, "author"), (Integer) v.get("totalCopies"), (Integer) v.get("availableCopies"));
            case "BookLoan" -> new BookLoan(str(v, "loanId"), (Student) v.get("student"),
                    (LibraryBook) v.get("book"), dt(v, "borrowedDate"), dt(v, "dueDate"), null);
            default -> throw new IllegalArgumentException("Unknown entity " + e);
        };
    }

    /** Re-checks uniqueness and reference existence immediately before saving. */
    private static void recheck(String[][] fields, Map<String, Object> vals, Object self)
            throws ValidationException {
        for (String[] f : fields) {
            String rule = ruleOf(f);
            Object v = vals.get(f[1]);
            if (f[4].replace("!", "").startsWith("u:") && v != null) {
                checkUnique(f[1], f[2], v, self);
            }
            if (rule.startsWith("ref,")) {
                if (v == null) {
                    throw new ValidationException(f[2] + " is required.");
                }
                if (!allOf(rule.split(",")[1]).contains(v)) {
                    throw new RecordNotFoundException(f[2] + " no longer exists in the system.");
                }
            }
        }
    }

    private static Object save(String entity, String[][] fields, Map<String, Object> vals)
            throws ValidationException {
        recheck(fields, vals, null);
        Object obj = build(entity, vals);
        ((Registrable) obj).validate();
        crossCheck(entity, obj, null);
        store(entity, obj);
        return obj;
    }

    private static void saveUpdate(String entity, Object existing, String[][] fields, Map<String, Object> vals,
                                   Map<String, Object> snapshot) throws ValidationException {
        recheck(fields, vals, existing);
        applyValues(existing, fields, vals);
        try {
            ((Registrable) existing).validate();
            crossCheck(entity, existing, existing);
        } catch (ValidationException | RuntimeException e) {
            applyValues(existing, fields, snapshot); 
            throw e;
        }
    }

    private static void applyValues(Object target, String[][] fields, Map<String, Object> vals) {
        for (String[] f : fields) {
            if (!ruleOf(f).equals("derived")) {
                setter(target, f[1], vals.get(f[1]));
            }
        }
    }

    private static void crossCheck(String entity, Object obj, Object self) throws ValidationException {
        switch (entity) {
            case "Enrollment" -> {
                Enrollment n = (Enrollment) obj;
                if (n.isActive()) {
                    for (Enrollment e : enrollments) {
                        if (e != self && e.isActive() && e.getStudent() == n.getStudent()
                                && e.getCourse() == n.getCourse()
                                && e.getSemester().equalsIgnoreCase(n.getSemester())) {
                            throw new DuplicateRecordException(n.getStudent().getDisplayName()
                                    + " already has an active enrollment in " + n.getCourse().getCourseCode()
                                    + " for " + n.getSemester() + ".");
                        }
                    }
                }
            }
            case "Classroom" -> {
                Classroom c = (Classroom) obj;
                for (Classroom o : classrooms) {
                    if (o != self && o.getBuildingName().equalsIgnoreCase(c.getBuildingName())
                            && o.getRoomNumber().equalsIgnoreCase(c.getRoomNumber())) {
                        throw new DuplicateRecordException("Room " + c.getRoomNumber() + " in "
                                + c.getBuildingName() + " is already registered.");
                    }
                }
            }
            case "Assessment" -> {
                Assessment a = (Assessment) obj;
                if (self == null && !a.getEnrollment().isActive()) {
                    throw new ValidationException("Only an ACTIVE enrollment can be assessed.");
                }
                if (a.getAssessmentDate().isBefore(a.getEnrollment().getEnrollmentDate())) {
                    throw new ValidationException("Assessment date cannot be before the enrollment date ("
                            + a.getEnrollment().getEnrollmentDate() + ").");
                }
            }
            case "FeePayment" -> {
                FeePayment p = (FeePayment) obj;
                if (p.getPaymentDate().isBefore(p.getStudent().getAdmissionDate())) {
                    throw new ValidationException("Payment date cannot be before the student's admission date ("
                            + p.getStudent().getAdmissionDate() + ").");
                }
            }
            case "BookLoan" -> {
                BookLoan l = (BookLoan) obj;
                if (self == null && l.getBook().getAvailableCopies() <= 0) {
                    throw new BookUnavailableException("No copy of '" + l.getBook().getTitle()
                            + "' is available right now.");
                }
            }
            default -> {
                // no extra cross-record rule for this type
            }
        }
    }

    private static void store(String entity, Object obj) {
        switch (entity) {
            case "Department" -> departments.add((Department) obj);
            case "Student" -> students.add((Student) obj);
            case "Teacher" -> teachers.add((Teacher) obj);
            case "Administrator" -> administrators.add((Administrator) obj);
            case "Course" -> courses.add((Course) obj);
            case "Classroom" -> classrooms.add((Classroom) obj);
            case "Enrollment" -> enrollments.add((Enrollment) obj);
            case "Assessment" -> assessments.add((Assessment) obj);
            case "FeePayment" -> payments.add((FeePayment) obj);
            case "LibraryBook" -> books.add((LibraryBook) obj);
            case "BookLoan" -> {
                BookLoan loan = (BookLoan) obj;
                LibraryBook b = loan.getBook();
                if (b.getAvailableCopies() <= 0) {
                    throw new BookUnavailableException("No copy of '" + b.getTitle() + "' is available.");
                }
                b.setAvailableCopies(b.getAvailableCopies() - 1); // decrement exactly once, only on success
                loans.add(loan);
                System.out.println("  Stock updated: '" + b.getTitle() + "' now has " + b.getAvailableCopies()
                        + "/" + b.getTotalCopies() + " copies available.");
            }
            default -> throw new IllegalArgumentException("Unknown entity " + entity);
        }
    }

    // =================================================================== return book

    private static void returnBook() throws ValidationException {
        List<BookLoan> open = loans.stream().filter(BookLoan::isActive).toList();
        if (open.isEmpty()) {
            System.out.println("- There are no active loans to return.");
            return;
        }
        System.out.println("\n--- RETURN LIBRARY BOOK ---");
        BookLoan loan = (BookLoan) pick("Select the loan being returned", open);
        if (loan == null) {
            System.out.println("- Return cancelled. Nothing changed.");
            return;
        }
        LocalDate date;
        while (true) {
            String in = readLine("Returned date (LocalDate, YYYY-MM-DD, CANCEL to abort): ");
            if (in.equalsIgnoreCase("CANCEL")) {
                System.out.println("- Return cancelled. Nothing changed.");
                return;
            }
            try {
                date = LocalDate.parse(in);
                Registrable.requireNotFuture("Returned date", date);
                if (date.isBefore(loan.getBorrowedDate())) {
                    throw new ValidationException("Returned date cannot be before the borrowed date ("
                            + loan.getBorrowedDate() + ").");
                }
                break;
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date: use YYYY-MM-DD (example: 2026-11-17).");
            } catch (ValidationException e) {
                System.out.println("! " + e.getMessage());
            }
        }
        System.out.println("Loan: " + summary(loan) + "\nReturned on: " + date);
        if (!confirm("Confirm return? (YES/NO): ")) {
            System.out.println("- Return cancelled. Nothing changed.");
            return;
        }
        LibraryBook book = loan.getBook();
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new ValidationException("Stock error: all copies of '" + book.getTitle()
                    + "' are already marked available.");
        }
        loan.setReturnedDate(date);
        book.setAvailableCopies(book.getAvailableCopies() + 1); // increment exactly once
        System.out.println("+ Loan " + loan.getId() + " returned. '" + book.getTitle() + "' now has "
                + book.getAvailableCopies() + "/" + book.getTotalCopies() + " copies available.");
        if (date.isAfter(loan.getDueDate())) {
            System.out.println("  Note: returned " + ChronoUnit.DAYS.between(loan.getDueDate(), date) + " day(s) late.");
        }
    }

    // =================================================================== view / search / update

    private static Identifiable pick(String title, List<? extends Identifiable> list) {
        while (true) {
            System.out.println("---- " + title + " ----");
            for (int i = 0; i < list.size(); i++) {
                System.out.println(" " + (i + 1) + " " + summary(list.get(i)));
            }
            System.out.println(" 0 Cancel");
            int c = askInt("Select: ");
            if (c == 0) {
                return null;
            }
            if (c >= 1 && c <= list.size()) {
                return list.get(c - 1);
            }
            System.out.println("! Invalid choice.");
        }
    }

    private static String chooseEntityType(String title, boolean allowAll) {
        while (true) {
            System.out.println("---- " + title + " ----");
            for (int i = 0; i < ENTITIES.length; i++) {
                System.out.println(String.format(" %2d %s", i + 1, label(ENTITIES[i])));
            }
            if (allowAll) {
                System.out.println(" " + (ENTITIES.length + 1) + " All records");
            }
            System.out.println("  0 Back to main menu");
            int c = askInt("Select: ");
            if (c == 0) {
                return null;
            }
            if (c >= 1 && c <= ENTITIES.length) {
                return ENTITIES[c - 1];
            }
            if (allowAll && c == ENTITIES.length + 1) {
                return "ALL";
            }
            System.out.println("! Invalid choice.");
        }
    }

    private static void printList(String type) {
        List<? extends Identifiable> list = allOf(type);
        if (list.isEmpty()) {
            System.out.println("(No " + label(type) + " records have been registered yet.)");
            return;
        }
        System.out.println("---- " + label(type) + " records (" + list.size() + ") ----");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(" " + (i + 1) + ". " + summary(list.get(i)));
        }
    }

    private static void viewAll() {
        String type = chooseEntityType("VIEW ALL RECORDS", true);
        if (type == null) {
            return;
        }
        if (type.equals("ALL")) {
            if (allEntities().isEmpty()) {
                System.out.println("(The system is empty. Register a record first.)");
            }
            for (String t : ENTITIES) {
                if (!allOf(t).isEmpty()) {
                    printList(t);
                }
            }
        } else {
            printList(type);
        }
        readLine("Press ENTER to return to the main menu...");
    }

    private static void searchById() {
        String id = readLine("Enter the ID (or student/employee/staff/member number, course code, ISBN, receipt no.): ");
        if (id.isEmpty()) {
            throw new RecordNotFoundException("No ID was entered.");
        }
        String[] altKeys = {"studentNumber", "employeeNumber", "staffNumber", "memberNumber", "courseCode",
                "departmentCode", "receiptNumber", "isbn"};
        int found = 0;
        for (Identifiable e : allEntities()) {
            boolean hit = e.getId().equalsIgnoreCase(id);
            for (int i = 0; i < altKeys.length && !hit; i++) {
                Object v = getter(e, altKeys[i]);
                hit = v != null && v.toString().equalsIgnoreCase(id);
            }
            if (hit) {
                found++;
                System.out.println("\n>> " + e.getClass().getSimpleName() + " found");
                ((Registrable) e).printDetails();
            }
        }
        if (found == 0) {
            throw new RecordNotFoundException("No record with ID '" + id + "' exists.");
        }
    }

    private static void updateRecord() {
        String type = chooseEntityType("UPDATE EXISTING RECORD - choose the record type", false);
        if (type == null) {
            return;
        }
        List<? extends Identifiable> list = allOf(type);
        if (list.isEmpty()) {
            System.out.println("- No " + label(type) + " records exist, so there is nothing to update.");
            return;
        }
        if (type.equals("BookLoan")) {
            System.out.println("(Only the due date of a loan can be changed; use 'Return Library Book' to return it.)");
        }
        Identifiable target = pick("Select the " + label(type).toLowerCase() + " to update", list);
        if (target == null) {
            System.out.println("- Update cancelled.");
            return;
        }
        workflow(type, target);
    }

    // =================================================================== reports

    private static void studentSummary() {
        if (students.isEmpty()) {
            System.out.println("- No student is registered yet. Use option 2 to register a student first.");
            return;
        }
        Student s = (Student) pick("STUDENT ACADEMIC SUMMARY - select a student", students);
        if (s == null) {
            return;
        }
        System.out.println("\n=== ACADEMIC SUMMARY: " + s.getDisplayName() + " [" + s.getStudentNumber() + "] ===");
        System.out.println("Program: " + s.getProgramName() + " | Year " + s.getStudyYear() + " | Department: "
                + s.getDepartment().getDepartmentName() + " | " + s.getAcademicCategory());
        int mine = 0;
        int credits = 0;
        double sumObtained = 0;
        double sumMax = 0;
        for (Enrollment e : enrollments) {
            if (e.getStudent() != s) {
                continue;
            }
            mine++;
            if (!e.getEnrollmentStatus().equals("DROPPED")) {
                credits += e.getCourse().getCreditHours();
            }
            System.out.println("\n* " + e.getCourse().getDisplayName() + " (" + e.getCourse().getCreditHours()
                    + " cr) - " + e.getSemester() + " - " + e.getEnrollmentStatus());
            double o = 0;
            double m = 0;
            int n = 0;
            for (Assessment a : assessments) {
                if (a.getEnrollment() == e) {
                    n++;
                    o += a.getObtainedScore();
                    m += a.getMaximumScore();
                    System.out.printf("    - %-12s %6.2f / %-6.2f (%s)%n", a.getAssessmentType(),
                            a.getObtainedScore(), a.getMaximumScore(), a.getAssessmentDate());
                }
            }
            if (n == 0) {
                System.out.println("    (no assessments recorded)");
            } else {
                System.out.printf("    Course average: %.1f%%%n", 100.0 * o / m);
                sumObtained += o;
                sumMax += m;
            }
        }
        if (mine == 0) {
            System.out.println("\n(This student has no enrollments yet.)");
        } else {
            System.out.println("\nEnrollments: " + mine + " | Credit hours (not dropped): " + credits
                    + (sumMax > 0 ? String.format(" | Overall average: %.1f%%", 100.0 * sumObtained / sumMax) : ""));
        }
    }

    private static void paymentHistory() {
        if (students.isEmpty()) {
            System.out.println("- No student is registered yet. Use option 2 to register a student first.");
            return;
        }
        Student s = (Student) pick("STUDENT PAYMENT HISTORY - select a student", students);
        if (s == null) {
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        int n = 0;
        System.out.println("\n=== PAYMENTS: " + s.getDisplayName() + " [" + s.getStudentNumber() + "] ===");
        for (FeePayment p : payments) {
            if (p.getStudent() == s) {
                n++;
                total = total.add(p.getAmount());
                System.out.println(" " + n + ". " + summary(p));
            }
        }
        if (n == 0) {
            System.out.println("(No payments have been recorded for this student.)");
        } else {
            System.out.println("Total paid: " + total.toPlainString() + " in " + n + " payment(s).");
        }
    }

    private static void availableBooks() {
        List<LibraryBook> avail = books.stream().filter(b -> b.getAvailableCopies() > 0).toList();
        if (avail.isEmpty()) {
            System.out.println(books.isEmpty() ? "(No library books are registered yet.)"
                    : "(All books are currently on loan.)");
            return;
        }
        System.out.println("---- Available books (" + avail.size() + ") ----");
        for (int i = 0; i < avail.size(); i++) {
            System.out.println(" " + (i + 1) + ". " + summary(avail.get(i)));
        }
    }

    private static void demonstratePolymorphism() {
        System.out.println("\n=== POLYMORPHISM DEMONSTRATION (uses only records you registered) ===");
        System.out.println("Person and AcademicMember are abstract: they declare abstract methods "
                + "(getRoleDescription / getAcademicCategory),\nso `new Person(...)` or `new AcademicMember(...)` "
                + "does not compile - only concrete subclasses can be instantiated.");
        List<Person> people = new ArrayList<>();
        people.addAll(students);
        people.addAll(teachers);
        people.addAll(administrators);
        System.out.println("\n--- 1) List<Person>: one reference type, many runtime behaviours ---");
        if (people.isEmpty()) {
            System.out.println("(No Student, Teacher or Administrator registered yet - register some and run again.)");
        }
        for (Person p : people) {
            System.out.println("[" + p.getClass().getSimpleName() + "] role=" + p.getRoleDescription()
                    + " | " + p.getRegistrationSummary() + " | active=" + p.isActive());
            if (p instanceof AcademicMember m) {
                System.out.println("      AcademicMember category: " + m.getAcademicCategory());
            }
            p.printDetails(); // overridden chain: Student/Teacher -> AcademicMember -> Person via super.printDetails()
        }
        System.out.println("\n--- 2) List<Identifiable>: unrelated classes through one interface ---");
        List<Identifiable> things = allEntities();
        if (things.isEmpty()) {
            System.out.println("(No records registered yet.)");
        }
        for (Identifiable i : things) {
            System.out.println(String.format("%-14s id=%-14s display=%-40s active=%s",
                    i.getClass().getSimpleName(), i.getId(), i.getDisplayName(), i.isActive()));
        }
        System.out.println("\nKey ideas shown: constructor chaining with super(...), method overriding "
                + "(printDetails/validate call super first),\nruntime dispatch through Person and Identifiable references.");
    }
}
