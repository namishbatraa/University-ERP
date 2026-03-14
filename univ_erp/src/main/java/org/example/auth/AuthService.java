package org.example.auth;

import com.google.firebase.auth.FirebaseAuthException;
import org.example.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import org.example.service.FirebaseAuthService;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    // Authenticate user
    public String authenticate(String username, String password) {
        String userRole = null;
        String sql = "SELECT password_hash, role FROM users_auth WHERE username = ? AND status = 'ACTIVE'";

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
                    userRole = rs.getString("role").toLowerCase();
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

    public String sendOTPViaSMS(String username, String phoneNumber) throws Exception {
        if (!userExists(username)) {
            throw new SQLException("User not found");
        }

        String otp = FirebaseAuthService.sendOTPViaSMS(phoneNumber);

        String sql = "INSERT INTO otp_verification (username, otp_code, expires_at) " +
                "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 5 MINUTE))";

        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, otp);
            pstmt.executeUpdate();
        }

        return otp;
    }

    public boolean verifyOTP(String username, String otp) throws SQLException {
        String sql = "SELECT otp_id FROM otp_verification " +
                "WHERE username = ? AND otp_code = ? " +
                "AND is_used = FALSE AND expires_at > NOW()";

        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, otp);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int otpId = rs.getInt("otp_id");
                String updateSql = "UPDATE otp_verification SET is_used = TRUE WHERE otp_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, otpId);
                    updateStmt.executeUpdate();
                }
                return true;
            }
        }
        return false;
    }

    // Change password
    public void changePassword(String username, String newPassword) throws SQLException {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String sql = "UPDATE users_auth SET password_hash = ? WHERE username = ?";

        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    // Get phone number
    public String getPhoneNumber(String username) throws SQLException {
        String sql = "SELECT phone FROM users_auth WHERE username = ?";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("phone");
            }
        }
        return null;
    }

    // Check if user exists
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users_auth WHERE username = ?";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}

