package org.example.service;

import org.example.DatabaseConfig;
import java.sql.*;
import java.util.*;

public class StudentService {

    public int getStudentIdByUsername(String username) throws SQLException {
        String sql = "SELECT s.student_id FROM students_bach s " +
                "JOIN university_auth.users_auth u ON s.user_id = u.user_id " +
                "WHERE u.username = ?";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("student_id");
            }
        }
        throw new SQLException("Student not found for username: " + username);
    }

    public Map<String, String> getStudentProfile(int studentId) throws SQLException {
        String sql = "SELECT * FROM students_bach WHERE student_id = ?";
        Map<String, String> profile = new HashMap<>();

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                profile.put("roll_number", rs.getString("roll_number"));
                profile.put("first_name", rs.getString("first_name"));
                profile.put("last_name", rs.getString("last_name"));
                profile.put("email", rs.getString("email"));
                profile.put("program", rs.getString("program"));
                profile.put("year_of_study", String.valueOf(rs.getInt("year_of_study")));
                profile.put("status", rs.getString("status"));
            }
        }
        return profile;
    }

    public List<Map<String, String>> getEnrolledCourses(int studentId) throws SQLException {
        String sql = "SELECT c.course_code, c.title, " +
                "CONCAT(i.first_name, ' ', i.last_name) as instructor, " +
                "c.credits, s.schedule_day, s.schedule_time " +
                "FROM enrollments_bach e " +
                "JOIN sections_bach s ON e.section_id = s.section_id " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors_bach i ON s.instructor_id = i.instructor_id " +
                "WHERE e.student_id = ? AND e.status = 'ENROLLED'";

        List<Map<String, String>> courses = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> course = new HashMap<>();
                course.put("course_code", rs.getString("course_code"));
                course.put("title", rs.getString("title"));
                course.put("instructor", rs.getString("instructor"));
                course.put("credits", String.valueOf(rs.getInt("credits")));
                course.put("schedule", rs.getString("schedule_day") + " " + rs.getString("schedule_time"));
                courses.add(course);
            }
        }
        return courses;
    }

    public List<Map<String, String>> getGrades(int studentId) throws SQLException {
        String sql = "SELECT c.course_code, c.title, g.assessment_type, " +
                "g.obtained_score, g.max_score, g.weight_percentage, " +
                "e.grade_status, e.final_grade " + // Added these fields
                "FROM grades_bach g " +
                "JOIN enrollments_bach e ON g.enrollment_id = e.enrollment_id " +
                "JOIN sections_bach s ON e.section_id = s.section_id " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "WHERE e.student_id = ? " +
                "ORDER BY c.course_code, g.assessment_type";

        List<Map<String, String>> grades = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("course_code", rs.getString("course_code"));
                grade.put("course_title", rs.getString("title"));
                grade.put("assessment_type", rs.getString("assessment_type"));
                grade.put("obtained_score", String.valueOf(rs.getDouble("obtained_score")));
                grade.put("max_score", String.valueOf(rs.getDouble("max_score")));
                grade.put("weight", String.valueOf(rs.getDouble("weight_percentage")));
                grade.put("grade_status", rs.getString("grade_status"));
                grade.put("final_grade", rs.getString("final_grade")); // Added
                grades.add(grade);
            }
        }
        return grades;
    }


    // Get dashboard stats
    public Map<String, Integer> getDashboardStats(int studentId) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        Connection conn = DatabaseConfig.getErpConnection();

        try {
            String courseSql = "SELECT COUNT(*) as count FROM enrollments_bach " +
                    "WHERE student_id = ? AND status = 'ENROLLED'";
            try (PreparedStatement pstmt = conn.prepareStatement(courseSql)) {
                pstmt.setInt(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("enrolled_courses", rs.getInt("count"));
                }
            }

            stats.put("pending_assignments", 0);

            String gradeSql = "SELECT AVG((g.obtained_score/g.max_score)*100) as avg_grade " +
                    "FROM grades_bach g " +
                    "JOIN enrollments_bach e ON g.enrollment_id = e.enrollment_id " +
                    "WHERE e.student_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(gradeSql)) {
                pstmt.setInt(1, studentId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    double avgGrade = rs.getDouble("avg_grade");
                    stats.put("average_grade", (int) Math.round(avgGrade));
                } else {
                    stats.put("average_grade", 0);
                }
            }
        } finally {
            if (conn != null) conn.close();
        }

        return stats;
    }

    public List<Map<String, String>> getAvailableSections() throws SQLException {
        String sql = "SELECT s.section_id, c.course_code, c.title, s.section_number, " +
                "CONCAT(i.first_name, ' ', i.last_name) as instructor, " +
                "s.semester, s.year, s.schedule_day, s.schedule_time, " +
                "s.capacity, s.enrolled_count, s.registration_deadline " +
                "FROM sections_bach s " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors_bach i ON s.instructor_id = i.instructor_id " +
                "WHERE s.status = 'OPEN' AND s.enrolled_count < s.capacity " +
                "AND s.registration_deadline >= CURDATE() " +
                "ORDER BY c.course_code, s.section_number";

        List<Map<String, String>> sections = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> section = new HashMap<>();
                section.put("section_id", String.valueOf(rs.getInt("section_id")));
                section.put("course_code", rs.getString("course_code"));
                section.put("title", rs.getString("title"));
                section.put("section_number", rs.getString("section_number"));
                section.put("instructor", rs.getString("instructor"));
                section.put("semester", rs.getString("semester"));
                section.put("year", String.valueOf(rs.getInt("year")));
                section.put("schedule", rs.getString("schedule_day") + " " + rs.getString("schedule_time"));
                section.put("seats_available", String.valueOf(rs.getInt("capacity") - rs.getInt("enrolled_count")));
                sections.add(section);
            }
        }
        return sections;
    }

    public void enrollInSection(int studentId, int sectionId) throws SQLException {
        Connection conn = null;
        if( isMaintenanceModeOn()) {
            throw new SQLException("System is under maintenance. Please try again later.");
        }
        else {
            try {
                conn = DatabaseConfig.getErpConnection();
                conn.setAutoCommit(false);

                String checkSql = "SELECT COUNT(*) FROM enrollments_bach " +
                        "WHERE student_id = ? AND section_id = ? AND status = 'ENROLLED'";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, studentId);
                    checkStmt.setInt(2, sectionId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("Already enrolled in this section");
                    }
                }

                String capacitySql = "SELECT capacity, enrolled_count FROM sections_bach WHERE section_id = ?";
                try (PreparedStatement capacityStmt = conn.prepareStatement(capacitySql)) {
                    capacityStmt.setInt(1, sectionId);
                    ResultSet rs = capacityStmt.executeQuery();
                    if (rs.next()) {
                        int capacity = rs.getInt("capacity");
                        int enrolled = rs.getInt("enrolled_count");
                        if (enrolled >= capacity) {
                            throw new SQLException("Section is full");
                        }
                    }
                }

                String checkDroppedSql = "SELECT enrollment_id FROM enrollments_bach " +
                        "WHERE student_id = ? AND section_id = ? AND status = 'DROPPED'";
                int droppedEnrollmentId = -1;
                try (PreparedStatement checkDroppedStmt = conn.prepareStatement(checkDroppedSql)) {
                    checkDroppedStmt.setInt(1, studentId);
                    checkDroppedStmt.setInt(2, sectionId);
                    ResultSet rs = checkDroppedStmt.executeQuery();
                    if (rs.next()) {
                        droppedEnrollmentId = rs.getInt("enrollment_id");
                    }
                }

                if (droppedEnrollmentId > 0) {
                    String updateSql = "UPDATE enrollments_bach SET status = 'ENROLLED', " +
                            "enrollment_date = CURDATE() WHERE enrollment_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, droppedEnrollmentId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String enrollSql = "INSERT INTO enrollments_bach (student_id, section_id, enrollment_date, status) " +
                            "VALUES (?, ?, CURDATE(), 'ENROLLED')";
                    try (PreparedStatement enrollStmt = conn.prepareStatement(enrollSql)) {
                        enrollStmt.setInt(1, studentId);
                        enrollStmt.setInt(2, sectionId);
                        enrollStmt.executeUpdate();
                    }
                }

                String updateSql = "UPDATE sections_bach SET enrolled_count = enrolled_count + 1 WHERE section_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, sectionId);
                    updateStmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                if (conn != null) conn.rollback();
                throw new SQLException("Failed to enroll: " + e.getMessage(), e);
            } finally {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        }
    }

    // Drop a course (section)
    public void dropSection(int studentId, int sectionId) throws SQLException {
        Connection conn = null;
        if (isMaintenanceModeOn()) {
            throw new SQLException("System is under maintenance. Please try again later.");
        }
        else {
            try {
                conn = DatabaseConfig.getErpConnection();
                conn.setAutoCommit(false);

                String checkSql = "SELECT enrollment_id FROM enrollments_bach " +
                        "WHERE student_id = ? AND section_id = ? AND status = 'ENROLLED'";
                int enrollmentId = -1;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, studentId);
                    checkStmt.setInt(2, sectionId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        enrollmentId = rs.getInt("enrollment_id");
                    } else {
                        throw new SQLException("Not enrolled in this section");
                    }
                }

                String dropSql = "UPDATE enrollments_bach SET status = 'DROPPED' WHERE enrollment_id = ?";
                try (PreparedStatement dropStmt = conn.prepareStatement(dropSql)) {
                    dropStmt.setInt(1, enrollmentId);
                    dropStmt.executeUpdate();
                }

                // Decrease enrolled count
                String updateSql = "UPDATE sections_bach SET enrolled_count = enrolled_count - 1 WHERE section_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, sectionId);
                    updateStmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                if (conn != null) conn.rollback();
                throw new SQLException("Failed to drop: " + e.getMessage(), e);
            } finally {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        }
    }

    public List<Map<String, String>> getEnrolledSectionsWithIds(int studentId) throws SQLException {
        String sql = "SELECT e.section_id, c.course_code, c.title, s.section_number, " +
                "CONCAT(i.first_name, ' ', i.last_name) as instructor, " +
                "s.schedule_day, s.schedule_time " +
                "FROM enrollments_bach e " +
                "JOIN sections_bach s ON e.section_id = s.section_id " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors_bach i ON s.instructor_id = i.instructor_id " +
                "WHERE e.student_id = ? AND e.status = 'ENROLLED'";

        List<Map<String, String>> sections = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> section = new HashMap<>();
                section.put("section_id", String.valueOf(rs.getInt("section_id")));
                section.put("course_code", rs.getString("course_code"));
                section.put("title", rs.getString("title"));
                section.put("section_number", rs.getString("section_number"));
                section.put("instructor", rs.getString("instructor"));
                section.put("schedule", rs.getString("schedule_day") + " " + rs.getString("schedule_time"));
                sections.add(section);
            }
        }
        return sections;
    }



    // Get unread notification count
    public int getUnreadNotificationCount(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM notifications_bach " +
                "WHERE student_id = ? AND is_read = FALSE";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    // Get all notifications for a student
    public List<Map<String, String>> getNotifications(int studentId, boolean unreadOnly) throws SQLException {
        String sql = "SELECT * FROM notifications_bach WHERE student_id = ?";
        if (unreadOnly) {
            sql += " AND is_read = FALSE";
        }
        sql += " ORDER BY created_at DESC";

        List<Map<String, String>> notifications = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> notification = new HashMap<>();
                notification.put("notification_id", String.valueOf(rs.getInt("notification_id")));
                notification.put("type", rs.getString("notification_type"));
                notification.put("title", rs.getString("title"));
                notification.put("message", rs.getString("message"));
                notification.put("is_read", String.valueOf(rs.getBoolean("is_read")));
                notification.put("created_at", rs.getString("created_at"));
                notifications.add(notification);
            }
        }
        return notifications;
    }

    // Mark notification as read
    public void markNotificationAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications_bach SET is_read = TRUE WHERE notification_id = ?";
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        }
    }

    // Mark all notifications as read for a student
    public void markAllAsRead(int studentId) throws SQLException {
        String sql = "UPDATE notifications_bach SET is_read = TRUE WHERE student_id = ?";
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.executeUpdate();
        }
    }


    public List<Map<String, String>> getFinalizedGrades(int studentId) throws SQLException {
        List<Map<String, String>> finalGrades = new ArrayList<>();
        String sql = "SELECT " +
                "c.course_code, " +
                "c.title, " +
                "e.final_grade, " +
                "s.semester, " +
                "s.year " +
                "FROM enrollments_bach e " +
                "JOIN sections_bach s ON e.section_id = s.section_id " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "WHERE e.student_id = ? AND e.grade_status = 'FINALIZED'";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("course_code", rs.getString("course_code"));
                grade.put("title", rs.getString("title"));
                grade.put("cgpa", String.format("%.2f", rs.getDouble("final_grade")));
                grade.put("semester", rs.getString("semester"));
                grade.put("year", String.valueOf(rs.getInt("year")));
                finalGrades.add(grade);
            }
        }
        return finalGrades;
    }

    // Add notification (used internally when grades are posted or courses added)
    public void addNotification(int studentId, String type, String title, String message) throws SQLException {
        String sql = "INSERT INTO notifications_bach (student_id, notification_type, title, message) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, type);
            pstmt.setString(3, title);
            pstmt.setString(4, message);
            pstmt.executeUpdate();
        }
    }
    public boolean isMaintenanceModeOn() throws SQLException {
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT setting_value FROM settings_bach WHERE setting_key = 'maintenance_mode'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("setting_value"));
            }
            return false;
        }
    }

}
