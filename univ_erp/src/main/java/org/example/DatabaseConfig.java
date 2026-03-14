package org.example;

import java.io.*;
import java.sql.*;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try {
            InputStream input = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("application.properties");

            if (input == null) {
                input = new FileInputStream("application.properties");
            }

            if (input != null) {
                properties.load(input);
                System.out.println("✓ Properties file loaded successfully!");
            } else {
                System.err.println("✗ ERROR: application.properties not found!");
            }
        } catch (Exception ex) {
            System.err.println("✗ ERROR loading properties:");
            ex.printStackTrace();
        }
    }

    public static Connection getAuthConnection() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s/%s",
                properties.getProperty("auth.db.host"),
                properties.getProperty("auth.db.port"),
                properties.getProperty("auth.db.name"));
        System.out.println("Connecting to Auth DB: " + url);
        return DriverManager.getConnection(url,
                properties.getProperty("auth.db.username"),
                properties.getProperty("auth.db.password"));
    }

    public static Connection getErpConnection() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s/%s",
                properties.getProperty("erp.db.host"),
                properties.getProperty("erp.db.port"),
                properties.getProperty("erp.db.name"));
        System.out.println("Connecting to ERP DB: " + url);
        return DriverManager.getConnection(url,
                properties.getProperty("erp.db.username"),
                properties.getProperty("erp.db.password"));
    }


    public static String getAuthDbUser() {
        return "root"; // or your username
    }

    public static String getAuthDbPassword() {
        return "shashank";
    }

    public static String getAuthDbName() {
        return "university_auth";
    }

    public static String getErpDbUser() {
        return "root"; // or your username
    }

    public static String getErpDbPassword() {
        return "shashank";
    }

    public static String getErpDbName() {
        return "university_erp";
    }




}
