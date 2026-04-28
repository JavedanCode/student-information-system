package model;

public class Enrollment {

    private String studentUsername;
    private String courseCode;

    public Enrollment(String studentUsername, String courseCode) {
        this.studentUsername = studentUsername;
        this.courseCode = courseCode;
    }

    public String getStudentUsername() { return studentUsername; }
    public String getCourseCode() { return courseCode; }

    public String toFileString() {
        return studentUsername + "," + courseCode;
    }

    public static Enrollment fromFileString(String line) {
        String[] parts = line.split(",");
        return new Enrollment(parts[0], parts[1]);
    }
}