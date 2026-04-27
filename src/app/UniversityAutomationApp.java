package app;

import data.DataStore;
import gui.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class UniversityAutomationApp extends JFrame {

    private model.User currentUser;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public UniversityAutomationApp() {
        setTitle("Student Information System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add panels
        mainPanel.add(new LoginPanel(this), "LOGIN");
        mainPanel.add(new gui.AdminPanel(this), "ADMIN");
        mainPanel.add(new gui.InstructorPanel(this), "INSTRUCTOR");
        mainPanel.add(new gui.StudentPanel(this), "STUDENT");

        add(mainPanel);

        showPanel("LOGIN");
    }

    public void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }

    public void setCurrentUser(model.User user) {
        this.currentUser = user;
    }

    public model.User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {

        // Load data
        DataStore.getInstance().loadAll();

        SwingUtilities.invokeLater(() -> {
            new UniversityAutomationApp().setVisible(true);
        });
    }
}