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
    private DefaultTableModel courseModel;
    private DefaultTableModel tableModel;
    private JComboBox<String> instructorBox;
    private JPanel statsPanel;
    private JPanel bottom;

    public AdminPanel(UniversityAutomationApp app) {
        this.app = app;

        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Users", createUserPanel());
        tabs.addTab("Courses", createCoursePanel());

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> app.showPanel("LOGIN"));

        add(tabs, BorderLayout.CENTER);
    }

    // -----------------------------
    // USER PANEL
    // -----------------------------
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.putClientProperty("JComponent.arc", 20);

        JButton deleteBtn = new JButton("Delete Selected");

        // TABLE
        tableModel = new DefaultTableModel(new String[]{"Username", "Role", "Full Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // ONLY role editable
            }
        };
        userTable = new JTable(tableModel);

        userTable.setRowHeight(28);
        userTable.setShowGrid(false);
        userTable.setIntercellSpacing(new Dimension(0, 0));

        userTable.getModel().addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();

            if (row < 0 || column != 1) return; // only care about role column

            String username = (String) tableModel.getValueAt(row, 0);
            String newRoleStr = tableModel.getValueAt(row, 1).toString();

            DataStore ds = DataStore.getInstance();
            User user = ds.findUser(username);

            if (user == null) return;

            Role oldRole = user.getRole();

            try {
                Role newRole = Role.valueOf(newRoleStr);

                // no change → do nothing
                if (newRole == oldRole) return;

                // 🔥 prevent removing last admin via edit too
                if (oldRole == Role.ADMIN && newRole != Role.ADMIN) {
                    long adminCount = ds.getAllUsers().stream()
                            .filter(u -> u.getRole() == Role.ADMIN)
                            .count();

                    if (adminCount <= 1) {
                        JOptionPane.showMessageDialog(this,
                                "At least one admin must remain.");

                        // revert UI
                        tableModel.setValueAt(oldRole, row, 1);
                        return;
                    }
                }

                // apply change
                ds.getAllUsers().remove(user);
                ds.addUser(new User(
                        user.getUsername(),
                        user.getPassword(),
                        newRole,
                        user.getFullName(),
                        user.getReferenceId()
                ));

                ds.saveAll();

            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid role value");

                // 🔥 revert UI back to old value
                tableModel.setValueAt(oldRole, row, 1);
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.add(new JScrollPane(userTable), BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        refreshUserTable();

        JPanel topBar = new JPanel(new BorderLayout());

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.add(deleteBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.putClientProperty("JButton.buttonType", "roundRect");

        logoutBtn.addActionListener(e -> app.showPanel("LOGIN"));

        rightActions.add(logoutBtn);

        topBar.add(rightActions, BorderLayout.EAST);

        panel.add(topBar, BorderLayout.NORTH);

        // FORM
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JComboBox<Role> roleBox = new JComboBox<>(Role.values());

        usernameField.setPreferredSize(new Dimension(220,30));
        passwordField.setPreferredSize(new Dimension(220,30));
        fullNameField.setPreferredSize(new Dimension(220,30));
        roleBox.setPreferredSize(new Dimension(220,30));

        JButton addButton = new JButton("Add User");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Add User"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.LINE_END;

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0;
        form.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        form.add(new JLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(fullNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0;
        form.add(new JLabel("Role:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(roleBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        addButton.setPreferredSize(new Dimension(140, 36));


        form.add(addButton, gbc);


        bottom = new JPanel(new GridLayout(1, 2, 15, 0));

        JPanel formWrapper = new JPanel(new GridBagLayout());
        formWrapper.add(form);

        bottom.add(formWrapper);
        statsPanel = createStatsPanel();
        statsPanel.setPreferredSize(new Dimension(320, 250));
        JPanel statsWrapper = new JPanel(new GridBagLayout());
        statsWrapper.add(statsPanel);

        bottom.add(statsWrapper);

        panel.add(bottom, BorderLayout.SOUTH);

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
            refreshStats();

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
            Role role = (Role) tableModel.getValueAt(row, 1);

            DataStore ds = DataStore.getInstance();

            if (role == Role.ADMIN) {

                long adminCount = ds.getAllUsers().stream()
                        .filter(u -> u.getRole() == Role.ADMIN)
                        .count();

                if (adminCount <= 1) {
                    JOptionPane.showMessageDialog(this,
                            "You must have at least one admin in the system.");
                    return;
                }
            }


            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete user " + username + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                DataStore.getInstance().deleteUser(username);
                DataStore.getInstance().saveAll();

                refreshUserTable();
                refreshStats();

                if (instructorBox != null) {
                    refreshInstructorBox(instructorBox);
                }

                if (courseModel != null) {
                    refreshCourseTable(courseModel);
                }
            }
        });



        // Validation
        ValidationUtil.onlyAlphaNumeric(usernameField);

        ValidationUtil.onlyLetters(fullNameField);

        ValidationUtil.limitTextLength(passwordField, 20);

        return panel;
    }

    private void refreshStats() {
        bottom.remove(statsPanel);
        statsPanel = createStatsPanel();
        bottom.add(statsPanel);

        bottom.revalidate();
        bottom.repaint();
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

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.putClientProperty("JComponent.arc", 20);


        courseModel = new DefaultTableModel(
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
                refreshStats();

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


        addButton.putClientProperty("JButton.buttonType", "roundRect");
        addButton.putClientProperty("JButton.arc", 15);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.putClientProperty("JButton.buttonType", "roundRect");
        deleteBtn.putClientProperty("JButton.arc", 15);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.add(deleteBtn);

        panel.add(topBar, BorderLayout.NORTH);

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
                refreshStats();
            }
        });

        //Validation
        ValidationUtil.onlyAlphaNumeric(codeField);

        ValidationUtil.onlyLetters(nameField);

        ValidationUtil.onlyNumbers(creditField);
        ValidationUtil.onlyNumbers(quotaField);

        return panel;
    }

    private JPanel createStatCard(String label, long value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.putClientProperty("JComponent.arc", 20);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(textLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(2, 2, 15, 15));
        stats.setBorder(BorderFactory.createTitledBorder("System Overview"));
        stats.putClientProperty("JComponent.arc", 20);

        DataStore ds = DataStore.getInstance();

        long admins = ds.getAllUsers().stream().filter(u -> u.getRole() == Role.ADMIN).count();
        long instructors = ds.getAllUsers().stream().filter(u -> u.getRole() == Role.INSTRUCTOR).count();
        long students = ds.getAllUsers().stream().filter(u -> u.getRole() == Role.STUDENT).count();
        int courses = ds.getAllCourses().size();

        stats.add(createStatCard("Admins", admins));
        stats.add(createStatCard("Instructors", instructors));
        stats.add(createStatCard("Students", students));
        stats.add(createStatCard("Courses", courses));

        return stats;
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