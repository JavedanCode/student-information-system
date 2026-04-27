package app;

import data.DataStore;
import model.*;

public class UniversityAutomationApp {

    public static void main(String[] args) {

        DataStore ds = DataStore.getInstance();
        ds.initialize();

        ds.addUser(new User("student1", "1234", Role.STUDENT, "John Doe", "S1"));
        ds.addCourse(new Course("C101", "Math", 3, 2, "inst1"));

        boolean enrolled = ds.enrollStudent("student1", "C101");

        System.out.println("Enrolled: " + enrolled);

        ds.upsertGrade("student1", "C101", 70, 80);

        double gpa = ds.calculateGPA("student1");

        System.out.println("GPA: " + gpa);
    }
}