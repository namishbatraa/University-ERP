package org.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseConfig {

    public static void main(String[] args) {
        System.out.println("=== Testing Database Configuration ===\n");

        // Test Auth Database Connection
        testAuthDatabase();

        // Test ERP Database Connection
        testErpDatabase();

        System.out.println("=== Test Complete ===");
    }

    private static void testAuthDatabase() {
        System.out.println("1. Testing Auth Database Connection...");
        try (Connection conn = DatabaseConfig.getAuthConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("   ✓ Auth DB Connection: SUCCESS");

                // Try to query the users table
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM users_auth");
                if (rs.next()) {
                    int count = rs.getInt("total");
                    System.out.println("   ✓ Found " + count + " users in users_auth table");
                }

                // Try to fetch one sample user
                rs = stmt.executeQuery("SELECT username, role FROM users_auth LIMIT 1");
                if (rs.next()) {
                    System.out.println("   ✓ Sample User: " + rs.getString("username") +
                            " (Role: " + rs.getString("role") + ")");
                }

                rs.close();
                stmt.close();
            }
        } catch (Exception e) {
            System.out.println("   ✗ Auth DB Connection: FAILED");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }







    private static void testErpDatabase() {
        System.out.println("2. Testing ERP Database Connection...");
        try (Connection conn = DatabaseConfig.getErpConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("   ✓ ERP DB Connection: SUCCESS");

                Statement stmt = conn.createStatement();

                // Test students_bach table
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM students_bach");
                if (rs.next()) {
                    int count = rs.getInt("total");
                    System.out.println("   ✓ Found " + count + " students in students_bach table");
                }

                // Fetch sample student
                rs = stmt.executeQuery("SELECT first_name, last_name, email FROM students_bach LIMIT 1");
                if (rs.next()) {
                    System.out.println("   ✓ Sample Student: " + rs.getString("first_name") +
                            " " + rs.getString("last_name") +
                            " (" + rs.getString("email") + ")");
                }

                // Test courses_bach table
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM courses_bach");
                if (rs.next()) {
                    int count = rs.getInt("total");
                    System.out.println("   ✓ Found " + count + " courses in courses_bach table");
                }

                // Fetch sample course
                rs = stmt.executeQuery("SELECT course_code, title FROM courses_bach LIMIT 1");
                if (rs.next()) {
                    System.out.println("   ✓ Sample Course: " + rs.getString("course_code") +
                            " - " + rs.getString("title"));
                }

                // Test instructors_bach table
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM instructors_bach");
                if (rs.next()) {
                    int count = rs.getInt("total");
                    System.out.println("   ✓ Found " + count + " instructors in instructors_bach table");
                }

                // Test enrollments_bach table
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM enrollments_bach");
                if (rs.next()) {
                    int count = rs.getInt("total");
                    System.out.println("   ✓ Found " + count + " enrollments in enrollments_bach table");
                }

                // Test sections_bach table
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM sections_bach");
                if (rs.next()) {
                    int count = rs.getInt("total");
                    System.out.println("   ✓ Found " + count + " sections in sections_bach table");
                }

                rs.close();
                stmt.close();
            }
        } catch (Exception e) {
            System.out.println("   ✗ ERP DB Connection: FAILED");
            System.out.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

}
