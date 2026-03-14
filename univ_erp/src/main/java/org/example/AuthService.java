package org.example;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    public String authenticate(String username, String password) {
        String userRole = null;
        String sql = "SELECT password_hash, role FROM users_auth WHERE username = ?";

        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("DEBUG: Looking for username: " + username);

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                System.out.println("DEBUG: User found! Hash: " + storedHash.substring(0, 15) + "...");
                System.out.println("DEBUG: Typed password: " + password);

                if (BCrypt.checkpw(password, storedHash)) {
                    userRole = rs.getString("role");
                    System.out.println("DEBUG: ✓ Password MATCH! Role: " + userRole);
                } else {
                    System.out.println("DEBUG: ✗ Password MISMATCH!");
                }
            } else {
                System.out.println("DEBUG: ✗ User NOT FOUND in database!");
            }
        } catch (SQLException e) {
            System.out.println("DEBUG: Database error!");
            e.printStackTrace();
        }
        return userRole;
    }

}
