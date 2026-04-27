package gui;

import app.UniversityAutomationApp;

import javax.swing.*;
import java.awt.*;

public class StudentPanel extends JPanel {

    public StudentPanel(UniversityAutomationApp app) {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Student Dashboard", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton logoutButton = new JButton("Logout");

        logoutButton.addActionListener(e -> app.showPanel("LOGIN"));

        add(title, BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);
    }
}