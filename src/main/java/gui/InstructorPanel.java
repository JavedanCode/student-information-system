package gui;

import app.UniversityAutomationApp;
import data.DataStore;
import model.*;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorPanel extends JPanel {

    private UniversityAutomationApp app;

    private JTable courseTable;
    private JTable studentTable;

    private DefaultTableModel courseModel;
    private DefaultTableModel studentModel;

    private JTextField midtermField;
    private JTextField finalField;



    public InstructorPanel(UniversityAutomationApp app) {
        this.app = app;



        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createCoursePanel(),
                createStudentPanel());

        splitPane.setDividerLocation(300);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> app.showPanel("LOGIN"));

        add(splitPane, BorderLayout.CENTER);
        add(logout, BorderLayout.SOUTH);
    }

    private void fillExistingGrade() {

        int studentRow = studentTable.getSelectedRow();
        int courseRow = courseTable.getSelectedRow();

        if (studentRow == -1 || courseRow == -1) return;

        String studentUsername = (String) studentModel.getValueAt(studentRow, 0);
        String courseCode = (String) courseModel.getValueAt(courseRow, 0);

        GradeRecord g = DataStore.getInstance().findGrade(studentUsername, courseCode);

        if (g != null) {
            midtermField.setText(String.valueOf(g.getMidterm()));
            finalField.setText(String.valueOf(g.getFinalExam()));
        } else {
            midtermField.setText("");
            finalField.setText("");
        }
    }

    // -----------------------------
    // LEFT: COURSES
    // -----------------------------
    private JPanel createCoursePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        courseModel = new DefaultTableModel(
                new String[]{"Course Code", "Course Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };


        courseTable = new JTable(courseModel);

        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseTable.setColumnSelectionAllowed(false);

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshStudents();
                midtermField.setText("");
                finalField.setText("");
                fillExistingGrade();

            }
        });

        panel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        return panel;
    }

    // -----------------------------
    // RIGHT: STUDENTS + GRADING
    // -----------------------------
    private JPanel createStudentPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        studentModel = new DefaultTableModel(
                new String[]{"Username", "Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(studentModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setColumnSelectionAllowed(false);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillExistingGrade();
            }
        });

        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // GRADE INPUT
        JPanel gradePanel = new JPanel(new GridLayout(3, 2, 10, 10));

        midtermField = new JTextField();
        finalField = new JTextField();

        ValidationUtil.onlyFloat(midtermField);
        ValidationUtil.onlyFloat(finalField);

        JButton saveGradeBtn = new JButton("Save Grade");

        gradePanel.add(new JLabel("Midterm:"));
        gradePanel.add(midtermField);

        gradePanel.add(new JLabel("Final:"));
        gradePanel.add(finalField);

        gradePanel.add(new JLabel(""));
        gradePanel.add(saveGradeBtn);

        panel.add(gradePanel, BorderLayout.SOUTH);

        // ACTION
        saveGradeBtn.addActionListener(e -> saveGrade());

        return panel;
    }

    // -----------------------------
    // REFRESH COURSES
    // -----------------------------
    public void refreshCourses() {
        if (courseModel == null || app.getCurrentUser() == null) return;

        courseModel.setRowCount(0);

        String instructorUsername = app.getCurrentUser().getUsername();

        List<Course> courses = DataStore.getInstance()
                .getCoursesByInstructor(instructorUsername);

        for (Course c : courses) {
            courseModel.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName()
            });
        }
    }

    // -----------------------------
    // REFRESH STUDENTS
    // -----------------------------
    private void refreshStudents() {

        if (courseTable == null || studentModel == null) return;

        studentModel.setRowCount(0);

        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) return;

        String courseCode = (String) courseModel.getValueAt(selectedRow, 0);

        List<Enrollment> enrollments = DataStore.getInstance()
                .getEnrollmentsByCourse(courseCode);

        for (Enrollment e : enrollments) {

            User user = DataStore.getInstance().findUser(e.getStudentUsername());

            if (user != null) {
                studentModel.addRow(new Object[]{
                        user.getUsername(),
                        user.getFullName()
                });
            }
        }
    }

    // -----------------------------
    // SAVE GRADE
    // -----------------------------
    private void saveGrade() {

        int studentRow = studentTable.getSelectedRow();
        int courseRow = courseTable.getSelectedRow();

        if (studentRow == -1 || courseRow == -1) {
            JOptionPane.showMessageDialog(this, "Select student and course");
            return;
        }
        if (midtermField.getText().isEmpty() || finalField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both midterm and final grades");
            return;
        }

        try {
            String studentUsername = (String) studentModel.getValueAt(studentRow, 0);
            String courseCode = (String) courseModel.getValueAt(courseRow, 0);

            double midterm = Double.parseDouble(midtermField.getText());
            double finalExam = Double.parseDouble(finalField.getText());

            try {
                DataStore.getInstance()
                        .upsertGrade(studentUsername, courseCode, midterm, finalExam);

                DataStore.getInstance().saveAll();

                JOptionPane.showMessageDialog(this, "Grade saved");

                midtermField.setText("");
                finalField.setText("");

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }


        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Grades must be valid numbers (e.g. 75 or 82.5)");
        }
    }
}