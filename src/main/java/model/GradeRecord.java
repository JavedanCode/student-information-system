package model;

public class GradeRecord {

    private String studentUsername;
    private String courseCode;
    private double midterm;
    private double finalExam;

    public GradeRecord(String studentUsername, String courseCode, double midterm, double finalExam) {
        this.studentUsername = studentUsername;
        this.courseCode = courseCode;
        this.midterm = midterm;
        this.finalExam = finalExam;
    }

    public String getStudentUsername() { return studentUsername; }
    public String getCourseCode() { return courseCode; }
    public double getMidterm() { return midterm; }
    public double getFinalExam() { return finalExam; }

    public double calculateAverage() {
        return midterm * 0.4 + finalExam * 0.6;
    }

    public String getLetterGrade() {
        double avg = calculateAverage();

        if (avg >= 90) return "AA";
        if (avg >= 85) return "BA";
        if (avg >= 80) return "BB";
        if (avg >= 75) return "CB";
        if (avg >= 70) return "CC";
        if (avg >= 65) return "DC";
        if (avg >= 60) return "DD";
        if (avg >= 50) return "FD";
        return "FF";
    }

    public String toFileString() {
        return studentUsername + "," + courseCode + "," + midterm + "," + finalExam;
    }

    public static GradeRecord fromFileString(String line) {
        String[] parts = line.split(",");
        return new GradeRecord(
                parts[0],
                parts[1],
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3])
        );
    }
}