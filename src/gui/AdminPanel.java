package gui;

import app.UniversityAutomationApp;

import javax.swing.*;
import java.awt.*;

public class AdminPanel extends JPanel {

    public AdminPanel(UniversityAutomationApp app) {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Admin Dashboard", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));

        JButton logoutButton = new JButton("Logout");

        logoutButton.addActionListener(e -> app.showPanel("LOGIN"));

        add(title, BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);
    }
}