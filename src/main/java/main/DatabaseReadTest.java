package main;

import database.DatabaseManager;

public class DatabaseReadTest {

    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        db.printAllResults();
    }
}