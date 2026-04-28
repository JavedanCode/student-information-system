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

    // -----------------------------
    // HELPERS
    // -----------------------------
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

    public void refreshAll() {
        if (app.getCurrentUser() == null) return;

        refreshAvailable();
        refreshEnrolled();
    }

    private JPanel createTopBar(JButton... buttons) {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        for (JButton b : buttons) {
            b.putClientProperty("JButton.buttonType", "roundRect");
            rightActions.add(b);
        }

        JButton logout = new JButton("Logout");
        logout.putClientProperty("JButton.buttonType", "roundRect");
        logout.addActionListener(e -> app.showPanel("LOGIN"));

        rightActions.add(logout);

        topBar.add(rightActions, BorderLayout.EAST);

        return topBar;
    }

    // -----------------------------
    // CONSTRUCTOR
    // -----------------------------
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

        add(tabs, BorderLayout.CENTER);
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
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

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

        panel.add(createTopBar(enrollBtn), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
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
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));


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


        panel.add(createTopBar(dropBtn), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
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
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        gpaLabel = new JLabel("GPA: 0.0");


        panel.add(createTopBar(), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(gpaLabel, BorderLayout.SOUTH);

        return panel;
    }


}