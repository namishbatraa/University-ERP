package org.example.ui;

import org.example.service.InstructorService;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.knowm.xchart.*;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler.*;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.CategoryChart;





public class InstructorDashboard extends JFrame {
    private final String username;
    private final InstructorService instructorService;
    private int instructorId;
    private JPanel mainPanel;
    private Map<String, String> instructorProfile;

    public InstructorDashboard(String username) {
        super("Instructor Dashboard - " + username);
        this.username = username;
        this.instructorService = new InstructorService();

        // Load instructor data
        try {
            this.instructorId = instructorService.getInstructorIdByUsername(username);
            this.instructorProfile = instructorService.getInstructorProfile(instructorId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading instructor data: " + e.getMessage());
            dispose();
            return;
        }

        initUI();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
    }
    private JPanel createMarksChartPanel(List<Map<String, Object>> data) {
        List<String> names = new ArrayList<>();
        List<Double> marks = new ArrayList<>();
        for (Map<String, Object> row : data) {
            names.add((String) row.get("name"));
            marks.add((Double) row.get("marks"));
        }

        CategoryChart chart = new CategoryChartBuilder()
                .width(600).height(400)
                .title("Marks vs Students")
                .xAxisTitle("Student")
                .yAxisTitle("Marks")
                .build();

        chart.addSeries("Total Marks", names, marks);

        return new XChartPanel<>(chart);
    }



    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createNavigationPanel(), BorderLayout.WEST);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        showWelcomeScreen();
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(142, 68, 173));
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String fullName = instructorProfile.get("first_name") + " " + instructorProfile.get("last_name");

        JLabel titleLabel = new JLabel("👨‍🏫 Instructor Portal");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Welcome, " + fullName);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        leftPanel.add(userLabel, BorderLayout.SOUTH);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(logoutButton, BorderLayout.EAST);

        return header;
    }

    private JPanel createNavigationPanel() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(new Color(52, 73, 94));
        nav.setPreferredSize(new Dimension(200, 0));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        String[] menuItems = {
                "📊 Dashboard",
                "📚 My Courses",
                "👥 Students",
                "📝 Manage Grades",
                "👤 Profile"
        };

        for (String item : menuItems) {
            JButton btn = createNavButton(item);
            nav.add(btn);
            nav.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        return nav;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(52, 73, 94));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));

        btn.addActionListener(e -> handleNavigation(text));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(142, 68, 173));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 73, 94));
            }
        });

        return btn;
    }


    private void downloadGradesAsCSV(List<Map<String, String>> grades, String sectionName) {
        if (grades == null || grades.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no grades to download.", "No Data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Gradebook CSV");
        // Suggest a filename like "MA101-A_Fall_2025_grades.csv"
        fileChooser.setSelectedFile(new File(sectionName.replaceAll("[^a-zA-Z0-9.-]", "_") + "_grades.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                // Write CSV header
                writer.println("Roll Number,Student Name,Assessment,Obtained Score,Max Score,Weight %");

                // Write grade data
                for (Map<String, String> grade : grades) {
                    writer.printf("\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f\n",
                            grade.get("roll_number"),
                            grade.get("student_name"),
                            grade.get("assessment_type"),
                            Double.parseDouble(grade.get("obtained_score")),
                            Double.parseDouble(grade.get("max_score")),
                            Double.parseDouble(grade.get("weight")));
                }

                JOptionPane.showMessageDialog(this,
                        "Gradebook successfully downloaded to:\n" + fileToSave.getAbsolutePath(),
                        "Download Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void handleNavigation(String menuItem) {
        mainPanel.removeAll();

        try {
            if (menuItem.contains("Dashboard")) {
                showWelcomeScreen();
            } else if (menuItem.contains("My Courses")) {
                showMyCoursesPanel();
            } else if (menuItem.contains("Students")) {
                showStudentsPanel();
            } else if (menuItem.contains("Manage Grades")) {
                showManageGradesPanel();
            } else if (menuItem.contains("Profile")) {
                showProfilePanel();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // DASHBOARD
    private void showWelcomeScreen() {
        try {
            JPanel welcome = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel welcomeLabel = new JLabel("Welcome to Instructor Portal!");
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0; gbc.gridy = 0;
            welcome.add(welcomeLabel, gbc);

            JPanel statsPanel = createStatsPanel();
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            welcome.add(statsPanel, gbc);

            mainPanel.add(welcome, BorderLayout.CENTER);
        } catch (Exception e) {
            mainPanel.add(new JLabel("Error loading dashboard: " + e.getMessage()));
        }
    }

    private JPanel createStatsPanel() {
        try {
            Map<String, Integer> stats = instructorService.getDashboardStats(instructorId);

            JPanel statsContainer = new JPanel(new GridLayout(1, 3, 20, 0));
            statsContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

            statsContainer.add(createStatCard("Total Courses",
                    String.valueOf(stats.get("total_courses")), new Color(142, 68, 173)));
            statsContainer.add(createStatCard("Total Students",
                    String.valueOf(stats.get("total_students")), new Color(52, 152, 219)));
            statsContainer.add(createStatCard("Pending Grades",
                    String.valueOf(stats.get("pending_grades")), new Color(231, 126, 35)));

            return statsContainer;
        } catch (Exception e) {
            JPanel error = new JPanel();
            error.add(new JLabel("Error loading stats: " + e.getMessage()));
            return error;
        }
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // MY COURSES
    private void showMyCoursesPanel() {
        try {
            JLabel label = new JLabel("📚 My Course Sections", SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 18));
            mainPanel.add(label, BorderLayout.NORTH);

            List<Map<String, String>> sections = instructorService.getMyCourseSections(instructorId);

            if (sections.isEmpty()) {
                mainPanel.add(new JLabel("No courses assigned", SwingConstants.CENTER), BorderLayout.CENTER);
                return;
            }

            String[] columns = {"Section ID", "Course Code", "Title", "Section", "Semester", "Schedule", "Enrollment", "Status"};
            Object[][] data = new Object[sections.size()][8];

            for (int i = 0; i < sections.size(); i++) {
                Map<String, String> section = sections.get(i);
                data[i][0] = section.get("section_id");
                data[i][1] = section.get("course_code");
                data[i][2] = section.get("title");
                data[i][3] = section.get("section_number");
                data[i][4] = section.get("semester") + " " + section.get("year");
                data[i][5] = section.get("schedule");
                data[i][6] = section.get("enrollment");
                data[i][7] = section.get("status");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            JScrollPane scrollPane = new JScrollPane(table);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
        }
    }

    // STUDENTS
    private void showStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("👥 Students by Section", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        try {
            List<Map<String, String>> sections = instructorService.getMyCourseSections(instructorId);

            if (sections.isEmpty()) {
                panel.add(new JLabel("No courses assigned", SwingConstants.CENTER), BorderLayout.CENTER);
                mainPanel.add(panel);
                return;
            }

            JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel selectLabel = new JLabel("Select Section:");
            JComboBox<String> sectionCombo = new JComboBox<>();

            for (Map<String, String> section : sections) {
                sectionCombo.addItem(section.get("course_code") + " - " +
                        section.get("section_number") + " (" + section.get("semester") + " " +
                        section.get("year") + ")");
            }

            JButton viewButton = new JButton("View Students");
            viewButton.addActionListener(e -> {
                int index = sectionCombo.getSelectedIndex();
                if (index >= 0) {
                    int sectionId = Integer.parseInt(sections.get(index).get("section_id"));
                    showStudentList(sectionId);
                }
            });

            selectionPanel.add(selectLabel);
            selectionPanel.add(sectionCombo);
            selectionPanel.add(viewButton);

            panel.add(selectionPanel, BorderLayout.NORTH);
            mainPanel.add(panel);

        } catch (Exception e) {
            panel.add(new JLabel("Error: " + e.getMessage()), BorderLayout.CENTER);
            mainPanel.add(panel);
        }
    }

    private void showStudentList(int sectionId) {
        try {
            List<Map<String, String>> students = instructorService.getEnrolledStudents(sectionId);

            String[] columns = {"Student ID", "Roll Number", "Name", "Email", "Enrollment Date"};
            Object[][] data = new Object[students.size()][5];

            for (int i = 0; i < students.size(); i++) {
                Map<String, String> student = students.get(i);
                data[i][0] = student.get("student_id");
                data[i][1] = student.get("roll_number");
                data[i][2] = student.get("name");
                data[i][3] = student.get("email");
                data[i][4] = student.get("enrollment_date");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);

            JDialog dialog = new JDialog(this, "Student List", true);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
            dialog.setSize(800, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + e.getMessage());
        }
    }

    // MANAGE GRADES
    private void showManageGradesPanel() throws SQLException {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("📝 Manage Student Grades", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        JComboBox<String> sectionCombo = null;
        try {
            List<Map<String, String>> sections = instructorService.getMyCourseSections(instructorId);

            if (sections.isEmpty()) {
                panel.add(new JLabel("No courses assigned", SwingConstants.CENTER), BorderLayout.CENTER);
                mainPanel.add(panel);
                return;
            }


            JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel selectLabel = new JLabel("Select Section:");
            sectionCombo = new JComboBox<>();

            for (Map<String, String> section : sections) {
                sectionCombo.addItem(section.get("course_code") + " - " +
                        section.get("section_number") + " (" + section.get("semester") + " " +
                        section.get("year") + ")");
            }

            JButton viewGradesButton = new JButton("View Grades");
            JButton addGradeButton = new JButton("Add/Update Grade");

            JComboBox<String> finalSectionCombo2 = sectionCombo;
            viewGradesButton.addActionListener(e -> {
                int index = finalSectionCombo2.getSelectedIndex();
                if (index >= 0) {
                    int sectionId = Integer.parseInt(sections.get(index).get("section_id"));
                    showGradesList(sectionId);
                }
            });

            JComboBox<String> finalSectionCombo1 = sectionCombo;
            addGradeButton.addActionListener(e -> {
                int index = finalSectionCombo1.getSelectedIndex();
                if (index >= 0) {
                    int sectionId = Integer.parseInt(sections.get(index).get("section_id"));
                    showAddGradeDialog(sectionId);
                }
            });


            JButton importGradesButton = new JButton("📤 Import from CSV");
            importGradesButton.setBackground(new Color(52, 152, 219));
            importGradesButton.setForeground(Color.WHITE);
            importGradesButton.setFocusPainted(false);

            JComboBox<String> finalSectionCombo3 = sectionCombo;
            importGradesButton.addActionListener(e -> {
                int index = finalSectionCombo3.getSelectedIndex();
                if (index < 0) {
                    JOptionPane.showMessageDialog(this, "Please select a section first.", "No Section Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int sectionId = Integer.parseInt(sections.get(index).get("section_id"));

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Grades CSV File");
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        String result = instructorService.importGradesFromCSV(sectionId, selectedFile);
                        JOptionPane.showMessageDialog(this,
                                "Grade import complete!\n" + result,
                                "Import Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                                "Error importing grades: " + ex.getMessage(),
                                "Import Failed",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            });
            JButton finalizeGradesButton = new JButton("Finalize Grades");
            finalizeGradesButton.setBackground(new Color(231, 76, 60));
            finalizeGradesButton.setForeground(Color.WHITE);
            finalizeGradesButton.setFocusPainted(false);

            // Action listener for the finalize button
            JComboBox<String> finalSectionCombo = sectionCombo;
            finalizeGradesButton.addActionListener(e -> {
                // 'sectionCombo' and 'sections' are now accessible here
                int index = finalSectionCombo.getSelectedIndex();
                if (index >= 0) {
                    try {
                        int sectionId = Integer.parseInt(sections.get(index).get("section_id"));
                        double totalWeight = instructorService.getTotalAssessmentWeight(sectionId);

                        if (totalWeight == 100.0) {
                            int confirm = JOptionPane.showConfirmDialog(this,
                                    "This will finalize grades for all students in this section. This action cannot be undone.\n\n" +
                                            "Are you sure you want to proceed?",
                                    "Confirm Grade Finalization",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);

                            if (confirm == JOptionPane.YES_OPTION) {
                                instructorService.finalizeGradesForSection(sectionId);
                                JOptionPane.showMessageDialog(this, "Grades have been successfully finalized!");
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Cannot finalize grades. The total assessment weight for this section is " + totalWeight + "%, not 100%.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error during grade finalization: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });

            selectionPanel.add(selectLabel);
            selectionPanel.add(sectionCombo);
            selectionPanel.add(viewGradesButton);
            selectionPanel.add(addGradeButton);
            selectionPanel.add(finalizeGradesButton);
            selectionPanel.add(importGradesButton);

            panel.add(selectionPanel, BorderLayout.NORTH);
            mainPanel.add(panel);

        } catch (Exception e) {
            panel.add(new JLabel("Error: " + e.getMessage()), BorderLayout.CENTER);
            mainPanel.add(panel);
        }
        int sectionId = -1;
        final List<Map<String, String>> sections = instructorService.getMyCourseSections(instructorId);
        if (sectionCombo.getItemCount() > 0 && sectionCombo.getSelectedIndex() >= 0) {
            sectionId = Integer.parseInt(sections.get(sectionCombo.getSelectedIndex()).get("section_id"));
        }

        if (sectionId != -1) {
            List<Map<String, Object>> marksData = instructorService.getStudentTotalMarks(sectionId);
            JPanel chartPanel = createMarksChartPanel(marksData);

            // Now add chartPanel to your layout. For example, below your selection/buttons:
            panel.add(chartPanel, BorderLayout.CENTER);
        }


    }

//    private void showGradesList(int sectionId) {
//        try {
//            List<Map<String, String>> grades = instructorService.getSectionGrades(sectionId);
//
//            if (grades.isEmpty()) {
//                JOptionPane.showMessageDialog(this, "No grades entered yet");
//                return;
//            }
//
//            String[] columns = {"Roll Number", "Student Name", "Assessment", "Score", "Max", "Weight %"};
//            Object[][] data = new Object[grades.size()][6];
//
//            for (int i = 0; i < grades.size(); i++) {
//                Map<String, String> grade = grades.get(i);
//                data[i][0] = grade.get("roll_number");
//                data[i][1] = grade.get("student_name");
//                data[i][2] = grade.get("assessment_type");
//                data[i][3] = grade.get("obtained_score");
//                data[i][4] = grade.get("max_score");
//                data[i][5] = grade.get("weight");
//            }
//
//
//
//
//            JTable table = new JTable(data, columns);
//            table.setRowHeight(25);
//
//
//            JButton downloadButton = new JButton("📥 Download as CSV");
//            downloadButton.setFont(new Font("Arial", Font.BOLD, 12));
//            downloadButton.setBackground(new Color(46, 204, 113));
//            downloadButton.setForeground(Color.WHITE);
//            downloadButton.setFocusPainted(false);
//
//            // Find the section name for the default filename
//            String sectionName = "";
//            try {
//                List<Map<String, String>> sections = instructorService.getMyCourseSections(instructorId);
//                for(Map<String, String> sec : sections) {
//                    if(Integer.parseInt(sec.get("section_id")) == sectionId) {
//                        sectionName = sec.get("course_code") + "-" + sec.get("section_number");
//                        break;
//                    }
//                }
//            } catch(Exception ex) {/* ignore */}
//
//
//            final String finalSectionName = sectionName;
//            downloadButton.addActionListener(e -> downloadGradesAsCSV(grades, finalSectionName));
//
//            // Create a panel to hold th
//            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//            southPanel.add(downloadButton);
//
//            JDialog dialog = new JDialog(this, "Grades List", true);
//            dialog.setLayout(new BorderLayout());
//            dialog.add(new JScrollPane(table), BorderLayout.CENTER);
//            dialog.setSize(900, 500);
//            dialog.setLocationRelativeTo(this);
//            dialog.setVisible(true);
//
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(this, "Error loading grades: " + e.getMessage());
//        }
//    }
private void showGradesList(int sectionId) {
    try {
        List<Map<String, String>> grades = instructorService.getSectionGrades(sectionId);

        if (grades.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No grades have been entered for this section yet.");
            return;
        }

        // Create the table to display grades
        String[] columns = {"Roll Number", "Student Name", "Assessment", "Score", "Max", "Weight %"};
        Object[][] data = new Object[grades.size()][6];

        for (int i = 0; i < grades.size(); i++) {
            Map<String, String> grade = grades.get(i);
            data[i][0] = grade.get("roll_number");
            data[i][1] = grade.get("student_name");
            data[i][2] = grade.get("assessment_type");
            data[i][3] = grade.get("obtained_score");
            data[i][4] = grade.get("max_score");
            data[i][5] = grade.get("weight");
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);

        // Create the Download button
        JButton downloadButton = new JButton("📥 Download as CSV");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 12));
        downloadButton.setBackground(new Color(46, 204, 113));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);

        // Find the section name for the default filename
        String sectionName = "";
        try {
            List<Map<String, String>> sections = instructorService.getMyCourseSections(instructorId);
            for(Map<String, String> sec : sections) {
                if(Integer.parseInt(sec.get("section_id")) == sectionId) {
                    sectionName = sec.get("course_code") + "-" + sec.get("section_number");
                    break;
                }
            }
        } catch(Exception ex) {/* ignore */}


        final String finalSectionName = sectionName;
        downloadButton.addActionListener(e -> downloadGradesAsCSV(grades, finalSectionName));

        // Create a panel to hold the button
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(downloadButton);

        // Create and configure the dialog
        JDialog dialog = new JDialog(this, "View Grades", true);
        dialog.setLayout(new BorderLayout(10,10));
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(southPanel, BorderLayout.SOUTH); // Add button panel to the bottom
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading grades: " + e.getMessage());
        e.printStackTrace();
    }
}

    private void showAddGradeDialog(int sectionId) {
        try {
            List<Map<String, String>> students = instructorService.getEnrolledStudents(sectionId);

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students enrolled in this section");
                return;
            }

            JDialog dialog = new JDialog(this, "Add/Update Grade", true);
            dialog.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Student selection
            String[] studentOptions = students.stream()
                    .map(s -> s.get("roll_number") + " - " + s.get("name"))
                    .toArray(String[]::new);
            JComboBox<String> studentCombo = new JComboBox<>(studentOptions);

            // Assessment type
            String[] assessments = {"Midterm", "Final", "Quiz", "Assignment", "Project", "Attendance"};
            JComboBox<String> assessmentCombo = new JComboBox<>(assessments);

            // Score fields
            JTextField obtainedScoreField = new JTextField(10);
            JTextField maxScoreField = new JTextField(10);
            JTextField weightField = new JTextField(10);

            gbc.gridx = 0; gbc.gridy = 0;
            dialog.add(new JLabel("Student:"), gbc);
            gbc.gridx = 1;
            dialog.add(studentCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            dialog.add(new JLabel("Assessment Type:"), gbc);
            gbc.gridx = 1;
            dialog.add(assessmentCombo, gbc);

            gbc.gridx = 0; gbc.gridy = 2;
            dialog.add(new JLabel("Obtained Score:"), gbc);
            gbc.gridx = 1;
            dialog.add(obtainedScoreField, gbc);

            gbc.gridx = 0; gbc.gridy = 3;
            dialog.add(new JLabel("Max Score:"), gbc);
            gbc.gridx = 1;
            dialog.add(maxScoreField, gbc);

            gbc.gridx = 0; gbc.gridy = 4;
            dialog.add(new JLabel("Weight %:"), gbc);
            gbc.gridx = 1;
            dialog.add(weightField, gbc);

            JButton saveButton = new JButton("Save Grade");
            saveButton.addActionListener(e -> {
                try {
                    int studentIndex = studentCombo.getSelectedIndex();
                    int studentId = Integer.parseInt(students.get(studentIndex).get("student_id"));
                    int enrollmentId = instructorService.getEnrollmentId(sectionId, studentId);

                    instructorService.addOrUpdateGrade(
                            enrollmentId,
                            (String) assessmentCombo.getSelectedItem(),
                            Double.parseDouble(obtainedScoreField.getText()),
                            Double.parseDouble(maxScoreField.getText()),
                            Double.parseDouble(weightField.getText())
                    );

                    JOptionPane.showMessageDialog(dialog, "Grade saved successfully!");
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            dialog.add(saveButton, gbc);

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // PROFILE
    private void showProfilePanel() {
        JPanel profile = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("👤 Instructor Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        profile.add(titleLabel, gbc);

        String[][] fields = {
                {"Employee ID:", instructorProfile.get("employee_id")},
                {"Name:", instructorProfile.get("first_name") + " " + instructorProfile.get("last_name")},
                {"Email:", instructorProfile.get("email")},
                {"Department:", instructorProfile.get("department")},
                {"Hire Date:", instructorProfile.get("hire_date")},
                {"Status:", instructorProfile.get("status")}
        };

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.gridwidth = 1;
            JLabel keyLabel = new JLabel(fields[i][0]);
            keyLabel.setFont(new Font("Arial", Font.BOLD, 14));
            profile.add(keyLabel, gbc);

            gbc.gridx = 1;
            JLabel valueLabel = new JLabel(fields[i][1]);
            valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            profile.add(valueLabel, gbc);
        }

        mainPanel.add(profile, BorderLayout.CENTER);
    }

    // LOGOUT
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new loginframe().setVisible(true));
        }
    }

}

