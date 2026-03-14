package org.example.service;

import org.example.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class AdminService {

    public List<Map<String, String>> getAllUsers() throws SQLException {
        String sql = "SELECT user_id, username, role, created_at FROM users_auth ORDER BY role, username";
        List<Map<String, String>> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getAuthConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("user_id", String.valueOf(rs.getInt("user_id")));
                user.put("username", rs.getString("username"));
                user.put("role", rs.getString("role"));
                user.put("created_at", rs.getString("created_at"));
                users.add(user);
            }
        }
        return users;
    }

    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users_auth WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public void createCompleteInstructor(String username, String password, String employeeId,
                                         String firstName, String lastName, String email,
                                         String department, String phone) throws SQLException {
        Connection authConn = null;
        Connection erpConn = null;

        try {
            authConn = DatabaseConfig.getAuthConnection();
            erpConn = DatabaseConfig.getErpConnection();

            authConn.setAutoCommit(false);
            erpConn.setAutoCommit(false);

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String userSql = "INSERT INTO users_auth (username, password_hash, role, email, phone) VALUES (?, ?, 'INSTRUCTOR', ?, ?)";
            int userId;

            try (PreparedStatement pstmt = authConn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to create user");
                }
            }

            String instructorSql = "INSERT INTO instructors_bach (user_id, employee_id, first_name, last_name, " +
                    "email, department, hire_date, status) VALUES (?, ?, ?, ?, ?, ?, CURDATE(), 'ACTIVE')";
            try (PreparedStatement pstmt = erpConn.prepareStatement(instructorSql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, employeeId);
                pstmt.setString(3, firstName);
                pstmt.setString(4, lastName);
                pstmt.setString(5, email);
                pstmt.setString(6, department);
                pstmt.executeUpdate();
            }

            authConn.commit();
            erpConn.commit();

        } catch (Exception e) {
            if (authConn != null) authConn.rollback();
            if (erpConn != null) erpConn.rollback();
            throw new SQLException("Failed to create instructor: " + e.getMessage(), e);
        } finally {
            if (authConn != null) {
                authConn.setAutoCommit(true);
                authConn.close();
            }
            if (erpConn != null) {
                erpConn.setAutoCommit(true);
                erpConn.close();
            }
        }
    }

    public void createCompleteStudent(String username, String password, String rollNumber,
                                      String firstName, String lastName, String email,
                                      String program, int year, String phone) throws SQLException {
        Connection authConn = null;
        Connection erpConn = null;

        try {
            authConn = DatabaseConfig.getAuthConnection();
            erpConn = DatabaseConfig.getErpConnection();

            authConn.setAutoCommit(false);
            erpConn.setAutoCommit(false);

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String userSql = "INSERT INTO users_auth (username, password_hash, role, email, phone) VALUES (?, ?, 'STUDENT', ?, ?)";
            int userId;

            try (PreparedStatement pstmt = authConn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, email);
                pstmt.setString(4, phone);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to create user");
                }
            }

            String studentSql = "INSERT INTO students_bach (user_id, roll_number, first_name, last_name, " +
                    "email, program, year_of_study, enrollment_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE(), 'ACTIVE')";
            try (PreparedStatement pstmt = erpConn.prepareStatement(studentSql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, rollNumber);
                pstmt.setString(3, firstName);
                pstmt.setString(4, lastName);
                pstmt.setString(5, email);
                pstmt.setString(6, program);
                pstmt.setInt(7, year);
                pstmt.executeUpdate();
            }

            authConn.commit();
            erpConn.commit();

        } catch (Exception e) {
            if (authConn != null) authConn.rollback();
            if (erpConn != null) erpConn.rollback();
            throw new SQLException("Failed to create student: " + e.getMessage(), e);
        } finally {
            if (authConn != null) {
                authConn.setAutoCommit(true);
                authConn.close();
            }
            if (erpConn != null) {
                erpConn.setAutoCommit(true);
                erpConn.close();
            }
        }
    }


    public void createAdmin(String username, String password) throws SQLException {
        String sql = "INSERT INTO users_auth (username, password_hash, role) VALUES (?, ?, 'ADMIN')";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DatabaseConfig.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
        }
    }


    public List<Map<String, String>> getAllStudents() throws SQLException {
        String sql = "SELECT s.*, u.username FROM students_bach s " +
                "JOIN university_auth.users_auth u ON s.user_id = u.user_id " +
                "ORDER BY s.roll_number";
        List<Map<String, String>> students = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> student = new HashMap<>();
                student.put("student_id", String.valueOf(rs.getInt("student_id")));
                student.put("username", rs.getString("username"));
                student.put("roll_number", rs.getString("roll_number"));
                student.put("first_name", rs.getString("first_name"));
                student.put("last_name", rs.getString("last_name"));
                student.put("email", rs.getString("email"));
                student.put("program", rs.getString("program"));
                student.put("year_of_study", String.valueOf(rs.getInt("year_of_study")));
                student.put("status", rs.getString("status"));
                students.add(student);
            }
        }
        return students;
    }


    public List<Map<String, String>> getAllInstructors() throws SQLException {
        String sql = "SELECT i.*, u.username FROM instructors_bach i " +
                "JOIN university_auth.users_auth u ON i.user_id = u.user_id " +
                "ORDER BY i.employee_id";
        List<Map<String, String>> instructors = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> instructor = new HashMap<>();
                instructor.put("instructor_id", String.valueOf(rs.getInt("instructor_id")));
                instructor.put("username", rs.getString("username"));
                instructor.put("employee_id", rs.getString("employee_id"));
                instructor.put("first_name", rs.getString("first_name"));
                instructor.put("last_name", rs.getString("last_name"));
                instructor.put("email", rs.getString("email"));
                instructor.put("department", rs.getString("department"));
                instructor.put("status", rs.getString("status"));
                instructors.add(instructor);
            }
        }
        return instructors;
    }

    public List<Map<String, String>> getAllCourses() throws SQLException {
        String sql = "SELECT * FROM courses_bach ORDER BY department, course_code";
        List<Map<String, String>> courses = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> course = new HashMap<>();
                course.put("course_id", String.valueOf(rs.getInt("course_id")));
                course.put("course_code", rs.getString("course_code"));
                course.put("title", rs.getString("title"));
                course.put("credits", String.valueOf(rs.getInt("credits")));
                course.put("department", rs.getString("department"));
                course.put("is_active", String.valueOf(rs.getBoolean("is_active")));
                courses.add(course);
            }
        }
        return courses;
    }

    public void addCourse(String courseCode, String title, String description,
                          int credits, String department) throws SQLException {
        String sql = "INSERT INTO courses_bach (course_code, title, description, credits, department) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseCode);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setInt(4, credits);
            pstmt.setString(5, department);
            pstmt.executeUpdate();
        }
    }

    public void deleteCourse(int courseId) throws SQLException {
        String sql = "DELETE FROM courses_bach WHERE course_id = ?";
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
        }
    }

    public List<Map<String, String>> getAllSections() throws SQLException {
        String sql = "SELECT s.*, c.course_code, c.title, " +
                "CONCAT(i.first_name, ' ', i.last_name) as instructor_name " +
                "FROM sections_bach s " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors_bach i ON s.instructor_id = i.instructor_id " +
                "ORDER BY s.year DESC, s.semester, c.course_code";
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
                section.put("instructor", rs.getString("instructor_name"));
                section.put("semester", rs.getString("semester"));
                section.put("year", String.valueOf(rs.getInt("year")));
                section.put("schedule_day", rs.getString("schedule_day"));
                section.put("schedule_time", rs.getString("schedule_time"));
                section.put("capacity", String.valueOf(rs.getInt("capacity")));
                section.put("enrolled", String.valueOf(rs.getInt("enrolled_count")));
                sections.add(section);
            }
        }
        return sections;
    }

    public void addSection(int courseId, int instructorId, String sectionNumber,
                           String semester, int year, String scheduleDay, String scheduleTime,
                           int capacity, String deadline) throws SQLException {
        String sql = "INSERT INTO sections_bach (course_id, instructor_id, section_number, " +
                "semester, year, schedule_day, schedule_time, capacity, registration_deadline) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, instructorId);
            pstmt.setString(3, sectionNumber);
            pstmt.setString(4, semester);
            pstmt.setInt(5, year);
            pstmt.setString(6, scheduleDay);
            pstmt.setString(7, scheduleTime);
            pstmt.setInt(8, capacity);
            pstmt.setString(9, deadline);
            pstmt.executeUpdate();
        }
    }

    public String backupDatabase(String backupDirectory) throws Exception {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFile = backupDirectory + File.separator + "backup_" + timestamp + ".sql";

        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception e) {
        }

        String dbUser = props.getProperty("db.username", "root");
        String dbPass = props.getProperty("db.password", "");

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-u" + dbUser,
                "-p" + dbPass,
                "--databases",
                "university_auth",
                "university_erp",
                "--result-file=" + backupFile
        );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            return backupFile;
        } else {
            throw new Exception("Backup failed with exit code: " + exitCode);
        }
    }

    public void restoreDatabase(String backupFile) throws Exception {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            }
        }

        String dbUser = props.getProperty("db.username", "root");
        String dbPass = props.getProperty("db.password", "");

        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-u" + dbUser,
                "-p" + dbPass
        );

        pb.redirectInput(new File(backupFile));
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new Exception("Restore failed with exit code: " + exitCode);
        }
    }

    public Map<String, Integer> getSystemStats() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        try (Connection conn = DatabaseConfig.getErpConnection()) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students_bach WHERE status='ACTIVE'")) {
                if (rs.next()) stats.put("total_students", rs.getInt(1));
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM instructors_bach WHERE status='ACTIVE'")) {
                if (rs.next()) stats.put("total_instructors", rs.getInt(1));
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM courses_bach WHERE is_active=1")) {
                if (rs.next()) stats.put("total_courses", rs.getInt(1));
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM enrollments_bach WHERE status='ENROLLED'")) {
                if (rs.next()) stats.put("total_enrollments", rs.getInt(1));
            }
        }

        return stats;
    }

    public List<Map<String, String>> getCoursesForDropdown() throws SQLException {
        String sql = "SELECT course_id, course_code, title FROM courses_bach WHERE is_active = 1 ORDER BY course_code";
        List<Map<String, String>> courses = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> course = new HashMap<>();
                course.put("course_id", String.valueOf(rs.getInt("course_id")));
                course.put("course_code", rs.getString("course_code"));
                course.put("title", rs.getString("title"));
                course.put("display", rs.getString("course_code") + " - " + rs.getString("title"));
                courses.add(course);
            }
        }
        return courses;
    }

    public List<Map<String, String>> getInstructorsForDropdown() throws SQLException {
        String sql = "SELECT instructor_id, first_name, last_name, department FROM instructors_bach " +
                "WHERE status = 'ACTIVE' ORDER BY last_name, first_name";
        List<Map<String, String>> instructors = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> instructor = new HashMap<>();
                instructor.put("instructor_id", String.valueOf(rs.getInt("instructor_id")));
                instructor.put("first_name", rs.getString("first_name"));
                instructor.put("last_name", rs.getString("last_name"));
                instructor.put("department", rs.getString("department"));
                instructor.put("display", rs.getString("first_name") + " " + rs.getString("last_name") +
                        " (" + rs.getString("department") + ")");
                instructors.add(instructor);
            }
        }
        return instructors;
    }

    public void assignInstructorToCourse(int courseId, int instructorId, String sectionNumber,
                                         String semester, int year, String scheduleDay,
                                         String scheduleTime, int capacity, String deadline) throws SQLException {
        String sql = "INSERT INTO sections_bach (course_id, instructor_id, section_number, " +
                "semester, year, schedule_day, schedule_time, capacity, registration_deadline) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            pstmt.setInt(2, instructorId);
            pstmt.setString(3, sectionNumber);
            pstmt.setString(4, semester);
            pstmt.setInt(5, year);
            pstmt.setString(6, scheduleDay);
            pstmt.setString(7, scheduleTime);
            pstmt.setInt(8, capacity);
            pstmt.setString(9, deadline);
            pstmt.executeUpdate();
        }
    }

    public void updateSectionInstructor(int sectionId, int instructorId) throws SQLException {
        String sql = "UPDATE sections_bach SET instructor_id = ? WHERE section_id = ?";

        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, instructorId);
            pstmt.setInt(2, sectionId);
            pstmt.executeUpdate();
        }
    }

    public List<Map<String, String>> getCourseInstructorAssignments() throws SQLException {
        String sql = "SELECT s.section_id, c.course_code, c.title as course_title, " +
                "CONCAT(i.first_name, ' ', i.last_name) as instructor_name, " +
                "s.section_number, s.semester, s.year, s.schedule_day, s.schedule_time, " +
                "s.capacity, s.enrolled_count, i.instructor_id " +
                "FROM sections_bach s " +
                "JOIN courses_bach c ON s.course_id = c.course_id " +
                "LEFT JOIN instructors_bach i ON s.instructor_id = i.instructor_id " +
                "ORDER BY s.year DESC, s.semester, c.course_code, s.section_number";

        List<Map<String, String>> assignments = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> assignment = new HashMap<>();
                assignment.put("section_id", String.valueOf(rs.getInt("section_id")));
                assignment.put("course_code", rs.getString("course_code"));
                assignment.put("course_title", rs.getString("course_title"));
                assignment.put("instructor_name", rs.getString("instructor_name"));
                assignment.put("instructor_id", String.valueOf(rs.getInt("instructor_id")));
                assignment.put("section_number", rs.getString("section_number"));
                assignment.put("semester", rs.getString("semester"));
                assignment.put("year", String.valueOf(rs.getInt("year")));
                assignment.put("schedule", rs.getString("schedule_day") + " " + rs.getString("schedule_time"));
                assignment.put("enrollment", rs.getInt("enrolled_count") + "/" + rs.getInt("capacity"));
                assignments.add(assignment);
            }
        }
        return assignments;
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

    public void setMaintenanceMode(boolean on) throws SQLException {
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE settings_bach SET setting_value = ? WHERE setting_key = 'maintenance_mode'")) {
            ps.setString(1, on ? "true" : "false");
            ps.executeUpdate();
        }
    }
    public void deleteSection(int sectionId) throws SQLException {

        String checkSql = "SELECT COUNT(*) as count FROM enrollments_bach WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, sectionId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                throw new SQLException("Cannot delete section: Students are enrolled in this section. Please remove enrollments first.");
            }
        }

        String deleteSql = "DELETE FROM sections_bach WHERE section_id = ?";
        try (Connection conn = DatabaseConfig.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
            pstmt.setInt(1, sectionId);
            int affected = pstmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Section not found or already deleted.");
            }
        }
    }

    /**
     * Creates a backup of both Auth and ERP databases
     * @param backupDirectory Directory to store backup files
     * @return Success message with backup file paths
     * @throws Exception If backup fails
     */

    public String createDatabaseBackup(String backupDirectory) throws Exception {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

        String authBackupFile = backupDirectory + "/auth_backup_" + timestamp + ".sql";
        String authCommand = String.format(
                "mysqldump -u%s -p%s %s -r %s",
                DatabaseConfig.getAuthDbUser(),
                DatabaseConfig.getAuthDbPassword(),
                DatabaseConfig.getAuthDbName(),
                authBackupFile
        );

        String erpBackupFile = backupDirectory + "/erp_backup_" + timestamp + ".sql";
        String erpCommand = String.format(
                "mysqldump -u%s -p%s %s -r %s",
                DatabaseConfig.getErpDbUser(),
                DatabaseConfig.getErpDbPassword(),
                DatabaseConfig.getErpDbName(),
                erpBackupFile
        );

        try {
            Process authProcess = Runtime.getRuntime().exec(authCommand);
            int authResult = authProcess.waitFor();
            if (authResult != 0) {
                throw new Exception("Auth database backup failed");
            }

            Process erpProcess = Runtime.getRuntime().exec(erpCommand);
            int erpResult = erpProcess.waitFor();
            if (erpResult != 0) {
                throw new Exception("ERP database backup failed");
            }

            return "Backup completed successfully:\n" +
                    "Auth DB: " + authBackupFile + "\n" +
                    "ERP DB: " + erpBackupFile;

        } catch (Exception e) {
            throw new Exception("Backup failed: " + e.getMessage());
        }
    }

    /**
     * Restores databases from backup files
     * @param authBackupFile Path to auth database backup file
     * @param erpBackupFile Path to ERP database backup file
     * @return Success message
     * @throws Exception If restore fails
     */
    public String restoreDatabaseBackup(String authBackupFile, String erpBackupFile) throws Exception {
        try {
            String authCommand = String.format(
                    "mysql -u%s -p%s %s < %s",
                    DatabaseConfig.getAuthDbUser(),
                    DatabaseConfig.getAuthDbPassword(),
                    DatabaseConfig.getAuthDbName(),
                    authBackupFile
            );

            Process authProcess = Runtime.getRuntime().exec(new String[]{"sh", "-c", authCommand});
            int authResult = authProcess.waitFor();
            if (authResult != 0) {
                throw new Exception("Auth database restore failed");
            }

            String erpCommand = String.format(
                    "mysql -u%s -p%s %s < %s",
                    DatabaseConfig.getErpDbUser(),
                    DatabaseConfig.getErpDbPassword(),
                    DatabaseConfig.getErpDbName(),
                    erpBackupFile
            );

            Process erpProcess = Runtime.getRuntime().exec(new String[]{"sh", "-c", erpCommand});
            int erpResult = erpProcess.waitFor();
            if (erpResult != 0) {
                throw new Exception("ERP database restore failed");
            }

            return "Database restored successfully!";

        } catch (Exception e) {
            throw new Exception("Restore failed: " + e.getMessage());
        }
    }



}
