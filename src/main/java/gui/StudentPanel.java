package gui;

import app.UniversityAutomationApp;
import data.DataStore;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentPanel extends JPanel {
    private JLabel gpaLabel;
    private UniversityAutomationApp app;

    private DefaultTableModel availableModel;
    private DefaultTableModel enrolledModel;
    private DefaultTableModel transcriptModel;

    public StudentPanel(UniversityAutomationApp app) {
        this.app = app;

        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addChangeListener(e -> {
            if (app.getCurrentUser() == null) return;

            refreshAvailable();
            refreshEnrolled();
            refreshTranscript();
        });

        tabs.addTab("Available Courses", createAvailablePanel());
        tabs.addTab("My Courses", createEnrolledPanel());
        tabs.addTab("Transcript", createTranscriptPanel());

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> app.showPanel("LOGIN"));

        add(tabs, BorderLayout.CENTER);
        add(logout, BorderLayout.SOUTH);
    }

    // -----------------------------
    // AVAILABLE COURSES
    // -----------------------------
    private JPanel createAvailablePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        availableModel = new DefaultTableModel(
                new String[]{"Code", "Name", "Quota"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(availableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);


        JButton enrollBtn = new JButton("Enroll Selected");

        enrollBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to enroll");
                return;
            }

            String courseCode = (String) availableModel.getValueAt(row, 0);
            String username = app.getCurrentUser().getUsername();

            boolean success = DataStore.getInstance()
                    .enrollStudent(username, courseCode);

            if (!success) {
                JOptionPane.showMessageDialog(this,
                        "Enrollment failed:\n- Already enrolled\n- Course full\n- Invalid course");
            } else {
                DataStore.getInstance().saveAll();

                refreshAll();
                refreshTranscript();

                JOptionPane.showMessageDialog(this, "Enrolled successfully");
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(enrollBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshAvailable() {
        if (availableModel == null || app.getCurrentUser() == null) return;
        availableModel.setRowCount(0);

        DataStore ds = DataStore.getInstance();

        for (Course c : ds.getAllCourses()) {
            int count = ds.countEnrollmentForCourse(c.getCourseCode());

            availableModel.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    (count) + " enrolled / " + c.getQuota()
            });
        }
    }

    // -----------------------------
    // ENROLLED COURSES
    // -----------------------------
    private JPanel createEnrolledPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        enrolledModel = new DefaultTableModel(
                new String[]{"Code", "Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(enrolledModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setColumnSelectionAllowed(false);


        JButton dropBtn = new JButton("Drop Selected");

        dropBtn.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a course to drop");
                return;
            }

            String courseCode = (String) enrolledModel.getValueAt(row, 0);
            String username = app.getCurrentUser().getUsername();

            DataStore.getInstance().removeEnrollment(username, courseCode);
            DataStore.getInstance().saveAll();

            refreshAll();
            refreshTranscript();

            JOptionPane.showMessageDialog(this, "Course dropped successfully");
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(dropBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshEnrolled() {
        if (enrolledModel == null || app.getCurrentUser() == null) return;
        enrolledModel.setRowCount(0);

        DataStore ds = DataStore.getInstance();
        String username = app.getCurrentUser().getUsername();

        List<Enrollment> list = ds.getEnrollmentsByStudent(username);

        for (Enrollment e : list) {
            Course c = ds.findCourse(e.getCourseCode());

            if (c != null) {
                enrolledModel.addRow(new Object[]{
                        c.getCourseCode(),
                        c.getCourseName()
                });
            }
        }
    }

    // -----------------------------
    // TRANSCRIPT
    // -----------------------------
    private JPanel createTranscriptPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        transcriptModel = new DefaultTableModel(
                new String[]{"Course", "Average", "Grade"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(transcriptModel);

        gpaLabel = new JLabel("GPA: 0.0");


        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(gpaLabel, BorderLayout.SOUTH);

        return panel;
    }

    public void refreshTranscript() {
        if (transcriptModel == null || gpaLabel == null || app.getCurrentUser() == null) return;

        transcriptModel.setRowCount(0);

        DataStore ds = DataStore.getInstance();
        String username = app.getCurrentUser().getUsername();

        for (GradeRecord g : ds.getGradesByStudent(username)) {

            Course c = ds.findCourse(g.getCourseCode());

            transcriptModel.addRow(new Object[]{
                    c != null ? c.getCourseName() : g.getCourseCode(),
                    g.calculateAverage(),
                    g.getLetterGrade()
            });
        }

        double gpa = ds.calculateGPA(username);
        gpaLabel.setText("GPA: " + String.format("%.2f", gpa));
    }

    public void refreshAll() {
        if (app.getCurrentUser() == null) return;

        refreshAvailable();
        refreshEnrolled();
    }
}