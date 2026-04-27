package gui;

import app.UniversityAutomationApp;
import data.DataStore;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {

    private UniversityAutomationApp app;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginPanel(UniversityAutomationApp app) {
        this.app = app;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Login");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");

        // Layout
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        add(userLabel, gbc);

        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(passLabel, gbc);

        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(loginButton, gbc);

        // Action
        loginButton.addActionListener(this::handleLogin);
    }

    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        User user = DataStore.getInstance().authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid credentials");
            return;
        }

        app.setCurrentUser(user);

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
}