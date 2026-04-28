package model;

public class Course {

    private String courseCode;
    private String courseName;
    private int credit;
    private int quota;
    private String instructorUsername;

    public Course(String courseCode, String courseName, int credit, int quota, String instructorUsername) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credit = credit;
        this.quota = quota;
        this.instructorUsername = instructorUsername;
    }

    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getCredit() { return credit; }
    public int getQuota() { return quota; }
    public String getInstructorUsername() { return instructorUsername; }

    public String toFileString() {
        return courseCode + "," + courseName + "," + credit + "," + quota + "," + instructorUsername;
    }

    public static Course fromFileString(String line) {
        String[] parts = line.split(",");
        return new Course(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                parts[4]
        );
    }
}