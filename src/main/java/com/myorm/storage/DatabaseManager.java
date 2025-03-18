package com.myorm.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "myorm_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";  // Change this to your MySQL password

    static {
        createDatabaseIfNotExists();
    }

    private static void createDatabaseIfNotExists() {
        try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("✅ Database created or already exists: " + DB_NAME);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL + DB_NAME, USERNAME, PASSWORD);
    }
}
