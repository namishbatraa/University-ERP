package org.example.service;

import org.example.DatabaseConfig;

import java.io.File;
import java.sql.*;
import java.util.*;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.sql.*;

public class InstructorService {

    public int getInstructorIdByUsername(String username) throws SQLException {
        String sql = "SELECT i.instructor_id FROM instructors_bach i " +
                "JOIN university_auth.users_auth u ON i.user_id = u.user_id " +
                "WHERE u.username = ?";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("instructor_id");
            }
        }
        throw new SQLException("Instructor not found for username: " + username);
    }

    public String importGradesFromCSV(int sectionId, File csvFile)
            throws SQLException, IOException, CsvValidationException {
        int gradesProcessed = 0;
        int gradesImported = 0;

        try (CSVReader reader = new CSVReader(new FileReader(String.valueOf(csvFile)))) {
            String[] header = reader.readNext();

            String[] line;
            while ((line = reader.readNext()) != null) {
                gradesProcessed++;
                try {
                    String rollNumber = line[0];
                    String assessmentType = line[2];
                    double obtainedScore = Double.parseDouble(line[3]);
                    double maxScore = Double.parseDouble(line[4]);
                    double weight = Double.parseDouble(line[5]);

                    int studentId = getStudentIdByRollNumber(rollNumber);
                    if (studentId == -1) {
                        continue;
                    }

                    int enrollmentId = getEnrollmentId(sectionId, studentId);

                    addOrUpdateGrade(enrollmentId, assessmentType, obtainedScore, maxScore, weight);
                    gradesImported++;

                } catch (NumberFormatException | SQLException e) {
                    System.err.println("Skipping invalid row: " + String.join(",", line));
                }
            }
        }
        return String.format("Processed %d rows. Successfully imported %d grades.", gradesProcessed, gradesImported);
    }

    /**
     * Helper method to get a student's ID from their roll number.
     *
     * @param rollNumber The student's roll number.
     * @return The student's ID, or -1 if not found.
     * @throws SQLException If a database error occurs.
     */
    private int getStudentIdByRollNumber(String rollNumber) throws SQLException {
        String sql = "SELECT student_id FROM students_bach WHERE roll_number = ?";
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rollNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("student_id");
            }
        }
        return -1;
    }

    public void finalizeGradesForSection(int sectionId) throws SQLException {
        if (getTotalAssessmentWeight(sectionId) != 100.0) {
            throw new SQLException("Cannot finalize. Total assessment weight for the section is not 100%.");
        }

        String sql = "UPDATE enrollments_bach e " +
                "JOIN ( " +
                "    SELECT " +
                "        e.enrollment_id, " +
                "        SUM(g.obtained_score / g.max_score * g.weight_percentage) as final_percentage " +
                "    FROM enrollments_bach e " +
                "    JOIN grades_bach g ON e.enrollment_id = g.enrollment_id " +
                "    WHERE e.section_id = ? " +
                "    GROUP BY e.enrollment_id " +
                ") as calculated_grades ON e.enrollment_id = calculated_grades.enrollment_id " +
                "SET " +
                "    e.final_grade = (calculated_grades.final_percentage / 100.0) * 10.0, " + // Convert to 10-point CGPA
                "    e.grade_status = 'FINALIZED', " +
                "    e.status = 'COMPLETED' " +
                "WHERE e.section_id = ?";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            pstmt.setInt(2, sectionId);
            pstmt.executeUpdate();
        }
    }




    public double getTotalAssessmentWeight(int sectionId) throws SQLException {
        String sql = "SELECT SUM(g.weight_percentage) as total_weight " +
                "FROM grades_bach g " +
                "JOIN enrollments_bach e ON g.enrollment_id = e.enrollment_id " +
                "WHERE e.section_id = ? " +
                "GROUP BY e.student_id " +
                "LIMIT 1"; // Check weight for one student, assuming it's consistent

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_weight");
            }
        }
        return 0.0;
    }




    // Get instructor profile
    public Map<String, String> getInstructorProfile(int instructorId) throws SQLException {
        String sql = "SELECT * FROM instructors_bach WHERE instructor_id = ?";
        Map<String, String> profile = new HashMap<>();

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                profile.put("employee_id", rs.getString("employee_id"));
                profile.put("first_name", rs.getString("first_name"));
                profile.put("last_name", rs.getString("last_name"));
                profile.put("email", rs.getString("email"));
                profile.put("department", rs.getString("department"));
                profile.put("status", rs.getString("status"));
                profile.put("hire_date", rs.getString("hire_date"));
            }
        }
        return profile;
    }

    // Get courses taught by instructor
    public List<Map<String, String>> getMyCourseSections(int instructorId) throws SQLException {
        String sql = "SELECT s.section_id, c.course_code, c.title, s.section_number, " +
                "s.semester, s.year, s.schedule_day, s.schedule_time, " +
                "s.capacity, s.enrolled_count, s.status " +
                "FROM sections_bach s " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "WHERE s.instructor_id = ? " +
                "ORDER BY s.year DESC, s.semester, c.course_code";

        List<Map<String, String>> sections = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> section = new HashMap<>();
                section.put("section_id", String.valueOf(rs.getInt("section_id")));
                section.put("course_code", rs.getString("course_code"));
                section.put("title", rs.getString("title"));
                section.put("section_number", rs.getString("section_number"));
                section.put("semester", rs.getString("semester"));
                section.put("year", String.valueOf(rs.getInt("year")));
                section.put("schedule", rs.getString("schedule_day") + " " + rs.getString("schedule_time"));
                section.put("enrollment", rs.getInt("enrolled_count") + "/" + rs.getInt("capacity"));
                section.put("status", rs.getString("status"));
                sections.add(section);
            }
        }
        return sections;
    }

    // Get students enrolled in a specific section
    public List<Map<String, String>> getEnrolledStudents(int sectionId) throws SQLException {
        String sql = "SELECT s.student_id, s.roll_number, s.first_name, s.last_name, " +
                "s.email, e.enrollment_date, e.status " +
                "FROM enrollments_bach e " +
                "JOIN students_bach s ON e.student_id = s.student_id " +
                "WHERE e.section_id = ? AND e.status = 'ENROLLED' " +
                "ORDER BY s.roll_number";

        List<Map<String, String>> students = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> student = new HashMap<>();
                student.put("student_id", String.valueOf(rs.getInt("student_id")));
                student.put("roll_number", rs.getString("roll_number"));
                student.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                student.put("email", rs.getString("email"));
                student.put("enrollment_date", rs.getString("enrollment_date"));
                students.add(student);
            }
        }
        return students;
    }

    // Get grades for a specific section
    public List<Map<String, String>> getSectionGrades(int sectionId) throws SQLException {
        String sql = "SELECT s.roll_number, s.first_name, s.last_name, " +
                "g.assessment_type, g.obtained_score, g.max_score, g.weight_percentage " +
                "FROM grades_bach g " +
                "JOIN enrollments_bach e ON g.enrollment_id = e.enrollment_id " +
                "JOIN students_bach s ON e.student_id = s.student_id " +
                "WHERE e.section_id = ? " +
                "ORDER BY s.roll_number, g.assessment_type";

        List<Map<String, String>> grades = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, String> grade = new HashMap<>();
                grade.put("roll_number", rs.getString("roll_number"));
                grade.put("student_name", rs.getString("first_name") + " " + rs.getString("last_name"));
                grade.put("assessment_type", rs.getString("assessment_type"));
                grade.put("obtained_score", String.valueOf(rs.getDouble("obtained_score")));
                grade.put("max_score", String.valueOf(rs.getDouble("max_score")));
                grade.put("weight", String.valueOf(rs.getDouble("weight_percentage")));
                grades.add(grade);
            }
        }
        return grades;
    }

    // Add/Update grade for a student
//    public void addOrUpdateGrade(int enrollmentId, String assessmentType, double obtainedScore,
//                                 double maxScore, double weightPercentage) throws SQLException {
//        // Check if grade exists
//        String checkSql = "SELECT grade_id FROM grades_bach " +
//                "WHERE enrollment_id = ? AND assessment_type = ?";
//
//        try (Connection conn = DatabaseConfig.getErpConnection();
//             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
//            checkStmt.setInt(1, enrollmentId);
//            checkStmt.setString(2, assessmentType);
//            ResultSet rs = checkStmt.executeQuery();
//
//            if (rs.next()) {
//                // Update existing grade
//                String updateSql = "UPDATE grades_bach SET obtained_score = ?, max_score = ?, " +
//                        "weight_percentage = ? WHERE grade_id = ?";
//                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
//                    updateStmt.setDouble(1, obtainedScore);
//                    updateStmt.setDouble(2, maxScore);
//                    updateStmt.setDouble(3, weightPercentage);
//                    updateStmt.setInt(4, rs.getInt("grade_id"));
//                    updateStmt.executeUpdate();
//                }
//            } else {
//                // Insert new grade
//                String insertSql = "INSERT INTO grades_bach (enrollment_id, assessment_type, " +
//                        "obtained_score, max_score, weight_percentage) VALUES (?, ?, ?, ?, ?)";
//                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
//                    insertStmt.setInt(1, enrollmentId);
//                    insertStmt.setString(2, assessmentType);
//                    insertStmt.setDouble(3, obtainedScore);
//                    insertStmt.setDouble(4, maxScore);
//                    insertStmt.setDouble(5, weightPercentage);
//                    insertStmt.executeUpdate();
//                }
//            }
//        }

    // Add/Update grade for a student (WITH NOTIFICATION)
    public void addOrUpdateGrade(int enrollmentId, String assessmentType, double obtainedScore,
                                 double maxScore, double weightPercentage) throws SQLException {
        Connection conn = null;

        if (isMaintenanceModeOn()) {
            throw new SQLException("System is under maintenance. Please try again later.");
        }
        else {
            try {
                conn = DatabaseConfig.getErpConnection();
                conn.setAutoCommit(false);

                // Get student_id from enrollment
                int studentId = -1;
                String getStudentSql = "SELECT student_id FROM enrollments_bach WHERE enrollment_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(getStudentSql)) {
                    stmt.setInt(1, enrollmentId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                    }
                }

                // Check if grade exists
                String checkSql = "SELECT grade_id FROM grades_bach " +
                        "WHERE enrollment_id = ? AND assessment_type = ?";

                boolean isUpdate = false;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, enrollmentId);
                    checkStmt.setString(2, assessmentType);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        isUpdate = true;
                        // Update existing grade
                        String updateSql = "UPDATE grades_bach SET obtained_score = ?, max_score = ?, " +
                                "weight_percentage = ? WHERE grade_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setDouble(1, obtainedScore);
                            updateStmt.setDouble(2, maxScore);
                            updateStmt.setDouble(3, weightPercentage);
                            updateStmt.setInt(4, rs.getInt("grade_id"));
                            updateStmt.executeUpdate();
                        }
                    } else {
                        // Insert new grade
                        String insertSql = "INSERT INTO grades_bach (enrollment_id, assessment_type, " +
                                "obtained_score, max_score, weight_percentage) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, enrollmentId);
                            insertStmt.setString(2, assessmentType);
                            insertStmt.setDouble(3, obtainedScore);
                            insertStmt.setDouble(4, maxScore);
                            insertStmt.setDouble(5, weightPercentage);
                            insertStmt.executeUpdate();
                        }
                    }
                }

                // Add notification for student
                if (studentId > 0) {
                    String notifSql = "INSERT INTO notifications_bach (student_id, notification_type, title, message) " +
                            "VALUES (?, 'GRADE_POSTED', ?, ?)";
                    try (PreparedStatement notifStmt = conn.prepareStatement(notifSql)) {
                        notifStmt.setInt(1, studentId);
                        notifStmt.setString(2, isUpdate ? "Grade Updated" : "New Grade Posted");
                        notifStmt.setString(3, String.format("Your grade for %s has been %s: %.2f/%.2f",
                                assessmentType, isUpdate ? "updated" : "posted", obtainedScore, maxScore));
                        notifStmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (Exception e) {
                if (conn != null) conn.rollback();
                throw new SQLException("Failed to save grade: " + e.getMessage(), e);
            } finally {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        }
    }




    // Get enrollment ID for a student in a section
    public int getEnrollmentId(int sectionId, int studentId) throws SQLException {
        String sql = "SELECT enrollment_id FROM enrollments_bach " +
                "WHERE section_id = ? AND student_id = ? AND status = 'ENROLLED'";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sectionId);
            pstmt.setInt(2, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("enrollment_id");
            }
        }
        throw new SQLException("Enrollment not found");
    }

    // Get dashboard statistics
    public Map<String, Integer> getDashboardStats(int instructorId) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        try (Connection conn = DatabaseConfig.getErpConnection()) {
            // Total courses teaching
            String courseSql = "SELECT COUNT(DISTINCT section_id) as count FROM sections_bach " +
                    "WHERE instructor_id = ? AND status = 'OPEN'";
            try (PreparedStatement pstmt = conn.prepareStatement(courseSql)) {
                pstmt.setInt(1, instructorId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("total_courses", rs.getInt("count"));
                }
            }

            // Total students
            String studentSql = "SELECT COUNT(DISTINCT e.student_id) as count " +
                    "FROM enrollments_bach e " +
                    "JOIN sections_bach s ON e.section_id = s.section_id " +
                    "WHERE s.instructor_id = ? AND e.status = 'ENROLLED'";
            try (PreparedStatement pstmt = conn.prepareStatement(studentSql)) {
                pstmt.setInt(1, instructorId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("total_students", rs.getInt("count"));
                }
            }

            // Pending grades (placeholder)
            stats.put("pending_grades", 0);
        }

        return stats;
    }


    public List<Map<String, Object>> getStudentTotalMarks(int sectionId) throws SQLException {
        String sql =
                "SELECT s.student_id, s.roll_number, s.first_name, s.last_name, " +
                        "COALESCE(SUM(g.obtained_score * g.weight_percentage / g.max_score),0) as total_marks " +
                        "FROM enrollments_bach e " +
                        "JOIN students_bach s ON e.student_id = s.student_id " +
                        "LEFT JOIN grades_bach g ON e.enrollment_id = g.enrollment_id " +
                        "WHERE e.section_id = ? AND (e.grade_status IS NULL OR e.grade_status <> 'FINALIZED') " +
                        "GROUP BY s.student_id, s.roll_number, s.first_name, s.last_name " +
                        "ORDER BY s.roll_number";

        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                map.put("name", name);
                map.put("marks", rs.getDouble("total_marks")); // On 100 scale if weights sum to 100
                results.add(map);
            }
        }
        return results;
    }
    // Returns true if maintenance_mode flag is 'true' in DB
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
