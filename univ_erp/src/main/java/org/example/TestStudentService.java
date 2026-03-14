package org.example;

import org.example.service.StudentService;
import java.util.*;

public class TestStudentService {
    public static void main(String[] args) {
        StudentService service = new StudentService();
        
        String testUsername = "stu1";

        System.out.println("==============================================");
        System.out.println("   Testing StudentService");
        System.out.println("==============================================\n");

        try {
            // Test 1: Get Student ID from Username
            System.out.println("1. Testing getStudentIdByUsername()");
            System.out.println("   Username: " + testUsername);
            int studentId = service.getStudentIdByUsername(testUsername);
            System.out.println("   ✓ Student ID: " + studentId);
            System.out.println();

            // Test 2: Get Student Profile
            System.out.println("2. Testing getStudentProfile()");
            Map<String, String> profile = service.getStudentProfile(studentId);
            if (!profile.isEmpty()) {
                System.out.println("   ✓ Profile loaded successfully:");
                System.out.println("     Roll Number: " + profile.get("roll_number"));
                System.out.println("     Name: " + profile.get("first_name") + " " + profile.get("last_name"));
                System.out.println("     Email: " + profile.get("email"));
                System.out.println("     Program: " + profile.get("program"));
                System.out.println("     Year: " + profile.get("year_of_study"));
                System.out.println("     Status: " + profile.get("status"));
            } else {
                System.out.println("   ✗ No profile data found");
            }
            System.out.println();

            // Test 3: Get Dashboard Stats
            System.out.println("3. Testing getDashboardStats()");
            Map<String, Integer> stats = service.getDashboardStats(studentId);
            System.out.println("   ✓ Dashboard stats:");
            System.out.println("     Enrolled Courses: " + stats.get("enrolled_courses"));
            System.out.println("     Pending Assignments: " + stats.get("pending_assignments"));
            System.out.println("     Average Grade: " + stats.get("average_grade") + "%");
            System.out.println();

            // Test 4: Get Enrolled Courses
            System.out.println("4. Testing getEnrolledCourses()");
            List<Map<String, String>> courses = service.getEnrolledCourses(studentId);
            System.out.println("   ✓ Found " + courses.size() + " enrolled courses:");
            if (courses.isEmpty()) {
                System.out.println("     (No courses enrolled yet)");
            } else {
                for (Map<String, String> course : courses) {
                    System.out.println("     - " + course.get("course_code") +
                            ": " + course.get("title"));
                    System.out.println("       Instructor: " + course.get("instructor"));
                    System.out.println("       Credits: " + course.get("credits"));
                    System.out.println("       Schedule: " + course.get("schedule"));
                    System.out.println();
                }
            }

            // Test 5: Get Grades
            System.out.println("5. Testing getGrades()");
            List<Map<String, String>> grades = service.getGrades(studentId);
            System.out.println("   ✓ Found " + grades.size() + " grade entries:");
            if (grades.isEmpty()) {
                System.out.println("     (No grades recorded yet)");
            } else {
                String currentCourse = "";
                for (Map<String, String> grade : grades) {
                    String courseCode = grade.get("course_code");
                    if (!courseCode.equals(currentCourse)) {
                        System.out.println("\n     Course: " + courseCode + " - " + grade.get("course_title"));
                        currentCourse = courseCode;
                    }
                    System.out.println("       " + grade.get("assessment_type") + ": " +
                            grade.get("obtained_score") + "/" + grade.get("max_score") +
                            " (Weight: " + grade.get("weight") + "%)");
                }
            }
            System.out.println();

            // Summary
            System.out.println("==============================================");
            System.out.println("   All Tests Completed Successfully!");
            System.out.println("==============================================");

        } catch (Exception e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
            System.out.println("\nStack trace:");
            e.printStackTrace();

            System.out.println("\nTroubleshooting:");
            System.out.println("1. Make sure 'student1' user exists in university_auth.users_auth");
            System.out.println("2. Ensure user_id in users_auth matches user_id in students_bach");
            System.out.println("3. Verify database connection settings in DatabaseConfig.java");
            System.out.println("4. Run the complete SQL schema provided earlier");
        }
    }
}
