package gui;

import app.UniversityAutomationApp;
import data.DataStore;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {

    private UniversityAutomationApp app;

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        User user = DataStore.getInstance().authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid credentials");
            return;
        }

        app.setCurrentUser(user);

        usernameField.setText("");
        passwordField.setText("");

        switch (user.getRole()) {
            case ADMIN -> app.showPanel("ADMIN");

            case INSTRUCTOR -> app.showPanel("INSTRUCTOR");

            case STUDENT -> {
                app.showPanel("STUDENT");
                app.getStudentPanel().refreshAll();
                app.getStudentPanel().refreshTranscript();
            }
        }
    }

    public LoginPanel(UniversityAutomationApp app) {
        this.app = app;

        setLayout(new GridBagLayout());

        JLabel title = new JLabel("Student Information System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        usernameField.setPreferredSize(new Dimension(220, 30));
        passwordField.setPreferredSize(new Dimension(220, 30));

        JButton loginButton = new JButton("Login");
        loginButton.setFocusPainted(false);
        loginButton.putClientProperty("JButton.buttonType", "roundRect");
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.putClientProperty("JButton.arc", 20);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(userLabel, gbc);

        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(passLabel, gbc);

        gbc.gridx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(loginButton, gbc);

        JPanel wrapper = new JPanel(new BorderLayout(10, 15));
        wrapper.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        wrapper.putClientProperty("JComponent.arc", 20);

        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(form, BorderLayout.CENTER);

        add(wrapper);

        loginButton.addActionListener(this::handleLogin);
        passwordField.addActionListener(e -> handleLogin(null));
    }


}