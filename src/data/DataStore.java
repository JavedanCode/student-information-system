package data;

import util.FileUtil;

import model.*;

import java.util.*;
import java.util.stream.Collectors;

public class DataStore {

    private final String USERS_FILE = "data/users.txt";
    private final String STUDENTS_FILE = "data/students.txt";
    private final String COURSES_FILE = "data/courses.txt";
    private final String ENROLLMENTS_FILE = "data/enrollments.txt";
    private final String GRADES_FILE = "data/grades.txt";

    private static DataStore instance;

    private List<User> users = new ArrayList<>();
    private List<StudentProfile> students = new ArrayList<>();
    private List<Course> courses = new ArrayList<>();
    private List<Enrollment> enrollments = new ArrayList<>();
    private List<GradeRecord> grades = new ArrayList<>();

    private DataStore() {}

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    // -----------------------------
    // INITIALIZE (for testing)
    // -----------------------------
    public void initialize() {
        users.add(new User("admin", "1234", Role.ADMIN, "System Admin", "A1"));
    }

    // -----------------------------
    // AUTH
    // -----------------------------
    public User authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username)
                        && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public User findUser(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // -----------------------------
    // STUDENTS
    // -----------------------------
    public StudentProfile findStudentProfileByUsername(String username) {
        return students.stream()
                .filter(s -> s.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // -----------------------------
    // COURSES
    // -----------------------------
    public Course findCourse(String courseCode) {
        return courses.stream()
                .filter(c -> c.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);
    }

    public List<Course> getCoursesByInstructor(String instructorUsername) {
        return courses.stream()
                .filter(c -> c.getInstructorUsername().equals(instructorUsername))
                .collect(Collectors.toList());
    }

    public int countEnrollmentForCourse(String courseCode) {
        return (int) enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode))
                .count();
    }

    // -----------------------------
    // ENROLLMENT
    // -----------------------------
    public boolean isStudentEnrolled(String studentUsername, String courseCode) {
        return enrollments.stream()
                .anyMatch(e -> e.getStudentUsername().equals(studentUsername)
                        && e.getCourseCode().equals(courseCode));
    }

    public boolean enrollStudent(String studentUsername, String courseCode) {

        Course course = findCourse(courseCode);
        if (course == null) return false;

        if (isStudentEnrolled(studentUsername, courseCode)) return false;

        if (countEnrollmentForCourse(courseCode) >= course.getQuota()) return false;

        System.out.println("ENROLLING: " + studentUsername + " -> " + courseCode);

        enrollments.add(new Enrollment(studentUsername, courseCode));

        System.out.println("TOTAL ENROLLMENTS: " + enrollments.size());

        return true;
    }

    public void removeEnrollment(String studentUsername, String courseCode) {
        enrollments.removeIf(e ->
                e.getStudentUsername().equals(studentUsername)
                        && e.getCourseCode().equals(courseCode)
        );
    }

    public List<Enrollment> getEnrollmentsByStudent(String studentUsername) {
        return enrollments.stream()
                .filter(e -> e.getStudentUsername().equals(studentUsername))
                .collect(Collectors.toList());
    }

    public List<Enrollment> getEnrollmentsByCourse(String courseCode) {
        return enrollments.stream()
                .filter(e -> e.getCourseCode().equals(courseCode))
                .collect(Collectors.toList());
    }

    // -----------------------------
    // GRADES
    // -----------------------------
    public GradeRecord findGrade(String studentUsername, String courseCode) {
        return grades.stream()
                .filter(g -> g.getStudentUsername().equals(studentUsername)
                        && g.getCourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);
    }

    public void upsertGrade(String studentUsername, String courseCode, double midterm, double finalExam) {
        GradeRecord existing = findGrade(studentUsername, courseCode);

        if (existing != null) {
            grades.remove(existing);
        }

        grades.add(new GradeRecord(studentUsername, courseCode, midterm, finalExam));
    }

    public List<GradeRecord> getGradesByStudent(String studentUsername) {
        return grades.stream()
                .filter(g -> g.getStudentUsername().equals(studentUsername))
                .collect(Collectors.toList());
    }

    public double calculateGPA(String studentUsername) {
        List<GradeRecord> studentGrades = getGradesByStudent(studentUsername);

        if (studentGrades.isEmpty()) return 0.0;

        double total = 0;

        for (GradeRecord g : studentGrades) {
            total += convertToGPA(g.getLetterGrade());
        }

        return total / studentGrades.size();
    }

    private double convertToGPA(String letter) {
        switch (letter) {
            case "AA": return 4.0;
            case "BA": return 3.5;
            case "BB": return 3.0;
            case "CB": return 2.5;
            case "CC": return 2.0;
            case "DC": return 1.5;
            case "DD": return 1.0;
            case "FD": return 0.5;
            default: return 0.0;
        }
    }

    // -----------------------------
    // ADD METHODS (ADMIN USE)
    // -----------------------------
    public void addUser(User user) {
        users.add(user);
    }

    public void addStudentProfile(StudentProfile student) {
        students.add(student);
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    // -----------------------------
    // GET ALL (for tables later)
    // -----------------------------
    public List<User> getAllUsers() { return users; }
    public List<StudentProfile> getAllStudents() { return students; }
    public List<Course> getAllCourses() { return courses; }

    public void loadAll() {
        loadUsers();
        loadStudents();
        loadCourses();
        loadEnrollments();
        loadGrades();
    }

    private void loadUsers() {
        users.clear();
        for (String line : FileUtil.readFile(USERS_FILE)) {
            users.add(User.fromFileString(line));
        }
    }

    private void loadStudents() {
        students.clear();
        for (String line : FileUtil.readFile(STUDENTS_FILE)) {
            students.add(StudentProfile.fromFileString(line));
        }
    }

    private void loadCourses() {
        courses.clear();
        for (String line : FileUtil.readFile(COURSES_FILE)) {
            courses.add(Course.fromFileString(line));
        }
    }

    private void loadEnrollments() {
        enrollments.clear();
        for (String line : FileUtil.readFile(ENROLLMENTS_FILE)) {
            enrollments.add(Enrollment.fromFileString(line));
        }
    }

    private void loadGrades() {
        grades.clear();
        for (String line : FileUtil.readFile(GRADES_FILE)) {
            grades.add(GradeRecord.fromFileString(line));
        }
    }

    public void saveAll() {
        saveUsers();
        saveStudents();
        saveCourses();
        saveEnrollments();
        saveGrades();
    }

    private void saveUsers() {
        List<String> lines = users.stream()
                .map(User::toFileString)
                .toList();
        FileUtil.writeFile(USERS_FILE, lines);
    }

    private void saveStudents() {
        List<String> lines = students.stream()
                .map(StudentProfile::toFileString)
                .toList();
        FileUtil.writeFile(STUDENTS_FILE, lines);
    }

    private void saveCourses() {
        List<String> lines = courses.stream()
                .map(Course::toFileString)
                .toList();
        FileUtil.writeFile(COURSES_FILE, lines);
    }

    private void saveEnrollments() {
        List<String> lines = enrollments.stream()
                .map(Enrollment::toFileString)
                .toList();
        FileUtil.writeFile(ENROLLMENTS_FILE, lines);
    }

    private void saveGrades() {
        List<String> lines = grades.stream()
                .map(GradeRecord::toFileString)
                .toList();
        FileUtil.writeFile(GRADES_FILE, lines);
    }
}