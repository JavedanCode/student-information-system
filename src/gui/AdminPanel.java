package gui;

import app.UniversityAutomationApp;
import data.DataStore;
import model.Role;
import model.User;
import model.Course;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminPanel extends JPanel {

    private UniversityAutomationApp app;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> instructorBox;

    public AdminPanel(UniversityAutomationApp app) {
        this.app = app;

        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Users", createUserPanel());
        tabs.addTab("Courses", createCoursePanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> app.showPanel("LOGIN"));

        add(tabs, BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);
    }

    // -----------------------------
    // USER PANEL
    // -----------------------------
    private JPanel createUserPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JButton deleteBtn = new JButton("Delete Selected");

        // TABLE
        tableModel = new DefaultTableModel(new String[]{"Username", "Role", "Full Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // ONLY role editable
            }
        };
        userTable = new JTable(tableModel);

        userTable.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (row < 0 || column < 0) return;

            String username = (String) tableModel.getValueAt(row, 0);
            String roleStr = tableModel.getValueAt(row, 1).toString();
            String fullName = tableModel.getValueAt(row, 2).toString();

            DataStore ds = DataStore.getInstance();
            User user = ds.findUser(username);

            if (user != null) {
                try {
                    Role newRole = Role.valueOf(roleStr);

                    // recreate user (since fields are private and no setters)
                    ds.getAllUsers().remove(user);
                    ds.addUser(new User(
                            user.getUsername(),
                            user.getPassword(),
                            newRole,
                            fullName,
                            user.getReferenceId()
                    ));

                    ds.saveAll();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid role value");
                }
            }
        });

        refreshUserTable();

        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        panel.add(deleteBtn, BorderLayout.NORTH);

        // FORM
        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JComboBox<Role> roleBox = new JComboBox<>(Role.values());

        JButton addButton = new JButton("Add User");

        form.add(new JLabel("Username:"));
        form.add(usernameField);

        form.add(new JLabel("Password:"));
        form.add(passwordField);

        form.add(new JLabel("Full Name:"));
        form.add(fullNameField);

        form.add(new JLabel("Role:"));
        form.add(roleBox);

        form.add(new JLabel(""));
        form.add(addButton);

        panel.add(form, BorderLayout.SOUTH);

        // ACTION
        addButton.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();
            Role role = (Role) roleBox.getSelectedItem();



            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required");
                return;
            }

            if (!DataStore.getInstance().isValidName(fullName)) {
                JOptionPane.showMessageDialog(this, "Name must contain only letters");
                return;
            }

            if (username.contains(",") || password.contains(",")) {
                JOptionPane.showMessageDialog(this, "Comma not allowed (file format restriction)");
                return;
            }

            if (username.length() > 15 || password.length() > 20 || fullName.length() > 30) {
                JOptionPane.showMessageDialog(this, "Input too long");
                return;
            }



            DataStore ds = DataStore.getInstance();

            if (ds.findUser(username) != null) {
                JOptionPane.showMessageDialog(this, "User already exists");
                return;
            }

            ds.addUser(new User(username, password, role, fullName, "N/A"));
            ds.saveAll();



            refreshUserTable();

            usernameField.setText("");
            passwordField.setText("");
            fullNameField.setText("");
            if (instructorBox != null) {
                refreshInstructorBox(instructorBox);
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a user to delete");
                return;
            }

            String username = (String) tableModel.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete user " + username + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                DataStore.getInstance().deleteUser(username);
                DataStore.getInstance().saveAll();

                refreshUserTable();
                if (instructorBox != null) {
                    refreshInstructorBox(instructorBox);
                }
            }
        });



        // Validation
        ValidationUtil.onlyAlphaNumeric(usernameField);

        ValidationUtil.onlyLetters(fullNameField);

        ValidationUtil.limitTextLength(passwordField, 20);

        return panel;
    }

    private void refreshUserTable() {
        tableModel.setRowCount(0);

        for (User u : DataStore.getInstance().getAllUsers()) {
            tableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getRole(),
                    u.getFullName()
            });
        }
    }

    // -----------------------------
    // COURSE PANEL
    // -----------------------------
    private JPanel createCoursePanel() {

        JPanel panel = new JPanel(new BorderLayout());


        DefaultTableModel courseModel = new DefaultTableModel(
                new String[]{"Code", "Name", "Credit", "Quota"},0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable courseTable = new JTable(courseModel);

        refreshCourseTable(courseModel);

        panel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));

        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField creditField = new JTextField();
        JTextField quotaField = new JTextField();
        instructorBox = new JComboBox<>();

        for (User u : DataStore.getInstance().getAllUsers()) {
            if (u.getRole() == Role.INSTRUCTOR) {
                instructorBox.addItem(u.getUsername());
            }
        }

        JButton addButton = new JButton("Add Course");

        form.add(new JLabel("Code:"));
        form.add(codeField);

        form.add(new JLabel("Name:"));
        form.add(nameField);

        form.add(new JLabel("Credit:"));
        form.add(creditField);

        form.add(new JLabel("Quota:"));
        form.add(quotaField);

        form.add(new JLabel("Instructor Username:"));
        form.add(instructorBox);

        form.add(new JLabel(""));
        form.add(addButton);

        panel.add(form, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                String name = nameField.getText().trim();
                int credit = Integer.parseInt(creditField.getText().trim());
                int quota = Integer.parseInt(quotaField.getText().trim());
                String instructor = (String) instructorBox.getSelectedItem();



                if (code.isEmpty() || name.isEmpty() || instructor == null || instructor.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields required");
                    return;
                }

                if (!DataStore.getInstance().isValidName(name)) {
                    JOptionPane.showMessageDialog(this, "Course name must contain only letters");
                    return;
                }

                if (code.length() > 10 || name.length() > 30) {
                    JOptionPane.showMessageDialog(this, "Input too long");
                    return;
                }


                if (credit <= 0 || quota <= 0) {
                    JOptionPane.showMessageDialog(this, "Credit and quota must be positive");
                    return;
                }

                if (credit > 5) {
                    JOptionPane.showMessageDialog(this, "Max credit is 5");
                    return;
                }

                if (quota > 100) {
                    JOptionPane.showMessageDialog(this, "Max quota is 100");
                    return;
                }

                if (DataStore.getInstance().findUser(instructor) == null) {
                    JOptionPane.showMessageDialog(this, "Instructor does not exist");
                    return;
                }

                DataStore ds = DataStore.getInstance();

                if (ds.findCourse(code) != null) {
                    JOptionPane.showMessageDialog(this, "Course already exists");
                    return;
                }

                ds.addCourse(new Course(code, name, credit, quota, instructor));
                ds.saveAll();

                refreshCourseTable(courseModel);

                if (instructorBox != null) {
                    refreshInstructorBox(instructorBox);
                }

                codeField.setText("");
                nameField.setText("");
                creditField.setText("");
                quotaField.setText("");
                if (instructorBox.getItemCount() > 0) {
                    instructorBox.setSelectedIndex(0);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Credit and Quota must be numbers");
            }
        });

        JButton deleteBtn = new JButton("Delete Selected");
        panel.add(deleteBtn, BorderLayout.NORTH);

        deleteBtn.addActionListener(e -> {
            int row = courseTable.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a course");
                return;
            }

            String code = (String) courseModel.getValueAt(row, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete course " + code + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                DataStore.getInstance().deleteCourse(code);
                DataStore.getInstance().saveAll();

                refreshCourseTable(courseModel);
            }
        });

        //Validation
        ValidationUtil.onlyAlphaNumeric(codeField);

        ValidationUtil.onlyLetters(nameField);

        ValidationUtil.onlyNumbers(creditField);
        ValidationUtil.onlyNumbers(quotaField);

        return panel;
    }

    private void refreshInstructorBox(JComboBox<String> instructorBox) {
        instructorBox.removeAllItems();

        for (User u : DataStore.getInstance().getAllUsers()) {
            if (u.getRole() == Role.INSTRUCTOR) {
                instructorBox.addItem(u.getUsername());
            }
        }
    }

    private void refreshCourseTable(DefaultTableModel model) {
        model.setRowCount(0);

        for (Course c : DataStore.getInstance().getAllCourses()) {
            model.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredit(),
                    c.getQuota()
            });
        }
    }
}