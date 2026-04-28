package app;

import data.DataStore;
import gui.LoginPanel;

import javax.swing.*;
import java.awt.*;

import com.formdev.flatlaf.FlatDarkLaf;

public class UniversityAutomationApp extends JFrame {

    private model.User currentUser;
    private gui.StudentPanel studentPanel;
    private gui.InstructorPanel instructorPanel;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public UniversityAutomationApp() {
        setTitle("Student Information System");
        setSize(1080, 720);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add panels
        studentPanel = new gui.StudentPanel(this);
        instructorPanel = new gui.InstructorPanel(this);
        mainPanel.add(new LoginPanel(this), "LOGIN");
        mainPanel.add(new gui.AdminPanel(this), "ADMIN");
        mainPanel.add(instructorPanel, "INSTRUCTOR");
        mainPanel.add(studentPanel, "STUDENT");

        add(mainPanel);

        showPanel("LOGIN");
    }

    public void showPanel(String name) {
        cardLayout.show(mainPanel, name);

        if (name.equals("STUDENT") && studentPanel != null) {
            studentPanel.refreshAll();
            studentPanel.refreshTranscript();
        }
        if (name.equals("INSTRUCTOR") && instructorPanel != null) {
            instructorPanel.refreshCourses();
        }
    }

    public void setCurrentUser(model.User user) {
        this.currentUser = user;
    }

    public model.User getCurrentUser() {
        return currentUser;
    }

    public gui.StudentPanel getStudentPanel() {
        return studentPanel;
    }

    public static void main(String[] args) {

        // Load data
        DataStore.getInstance().loadAll();

        try {
            FlatDarkLaf.setup(); // enables modern UI
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 15));
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("Button.arc", 15);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new UniversityAutomationApp().setVisible(true);
        });
    }
}