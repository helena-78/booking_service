package com.sportlink.scheduling;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class DatabaseInitializer {

    private static final String JDBC_URL_BASE = "jdbc:postgresql://localhost:5432/";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public static void initialize(String dbName) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL_BASE + "postgres", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "'");

            if (!rs.next()) {
                stmt.executeUpdate("CREATE DATABASE " + dbName);
                System.out.println("Database '" + dbName + "' created.");
            } else {
                System.out.println("Database '" + dbName + "' already exists.");
            }
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}