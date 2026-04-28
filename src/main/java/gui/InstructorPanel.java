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

    // -----------------------------
    // HELPERS
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

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    // -----------------------------
    // COURSE PANEL
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

        styleTable(courseTable);

        return panel;
    }

    // -----------------------------
    // STUDENT PANEL
    // -----------------------------
    private JPanel createStudentPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

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

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Enter Grades"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_END;

        midtermField = new JTextField();
        finalField = new JTextField();

        midtermField.setPreferredSize(new Dimension(200, 30));
        finalField.setPreferredSize(new Dimension(200, 30));

        ValidationUtil.onlyFloat(midtermField);
        ValidationUtil.onlyFloat(finalField);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Midterm:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(midtermField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Final:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        form.add(finalField, gbc);

        JButton saveGradeBtn = new JButton("Save Grade");
        saveGradeBtn.setPreferredSize(new Dimension(140, 36));
        saveGradeBtn.putClientProperty("JButton.buttonType", "roundRect");

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        form.add(saveGradeBtn, gbc);

        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.add(form);

        JPanel main = new JPanel(new BorderLayout(10, 10));

        main.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        main.add(formWrapper, BorderLayout.SOUTH);

        panel.add(main, BorderLayout.CENTER);

        saveGradeBtn.addActionListener(e -> saveGrade());

        styleTable(studentTable);

        return panel;
    }

    // -----------------------------
    // CONSTRUCTOR
    // -----------------------------
    public InstructorPanel(UniversityAutomationApp app) {
        this.app = app;

        setLayout(new BorderLayout());
        JButton logout = new JButton("Logout");
        logout.putClientProperty("JButton.buttonType", "roundRect");
        logout.addActionListener(e -> app.showPanel("LOGIN"));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightActions.add(logout);

        topBar.add(rightActions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createCoursePanel(),
                createStudentPanel());
        splitPane.setResizeWeight(0.3);
        splitPane.setDividerSize(6);

        splitPane.setDividerLocation(300);

        logout.addActionListener(e -> app.showPanel("LOGIN"));

        add(splitPane, BorderLayout.CENTER);
    }
}