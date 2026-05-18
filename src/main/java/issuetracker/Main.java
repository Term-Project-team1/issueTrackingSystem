package issuetracker;

import issuetracker.database.DatabaseInitializer;

public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();

        System.out.println("issueTrackingSystem started.");
    }
}