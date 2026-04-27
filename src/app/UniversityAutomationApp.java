package app;

import data.DataStore;

public class UniversityAutomationApp {

    public static void main(String[] args) {

        DataStore ds = DataStore.getInstance();

        // Load existing data
        ds.loadAll();

        System.out.println("System loaded successfully");

        // You can test saving
        ds.saveAll();
    }
}