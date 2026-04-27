package app;

import data.DataStore;
import gui.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class UniversityAutomationApp extends JFrame {

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

        add(mainPanel);

        showPanel("LOGIN");
    }

    public void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }

    public static void main(String[] args) {

        // Load data
        DataStore.getInstance().loadAll();

        SwingUtilities.invokeLater(() -> {
            new UniversityAutomationApp().setVisible(true);
        });
    }
}