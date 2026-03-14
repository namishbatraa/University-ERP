package org.example.ui;

import org.example.service.AdminService;
import org.example.ui.loginframe;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class admindashboard extends JFrame {
    private final String username;
    private final AdminService adminService;
    private JPanel mainPanel;

    public admindashboard(String username) {
        super("Admin Dashboard - " + username);
        this.username = username;
        this.adminService = new AdminService();
        initUI();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createNavigationPanel(), BorderLayout.WEST);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        showDashboard();
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(231, 76, 60));
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel titleLabel = new JLabel("🔐 Admin Control Panel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("Administrator: " + username);
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
        nav.setBackground(new Color(44, 62, 80));
        nav.setPreferredSize(new Dimension(220, 0));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        String[] menuItems = {
                "📊 Dashboard",
                "👥 User Management",
                "🎓 Student Management",
                "👨‍🏫 Instructor Management",
                "📚 Course Management",
                "🔗 Course Assignments",
                "📅 Timetable/Sections",
                "💾 Backup & Restore",
                "📈 Reports",
                "💾 Backup & RestorE"


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
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(44, 62, 80));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));

        btn.addActionListener(e -> handleNavigation(text));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(44, 62, 80));
            }
        });

        return btn;
    }

    private void handleNavigation(String menuItem) {
        mainPanel.removeAll();

        try {
            if (menuItem.contains("Dashboard")) {
                showDashboard();
            } else if (menuItem.contains("User Management")) {
                showUserManagement();
            } else if (menuItem.contains("Student Management")) {
                showStudentManagement();
            } else if (menuItem.contains("Instructor Management")) {
                showInstructorManagement();
            } else if (menuItem.contains("Course Management")) {
                showCourseManagement();
            } else if (menuItem.contains("Timetable")) {
                showTimetableManagement();
            } else if (menuItem.contains("Backup")) {
                showBackupRestore();
            } else if (menuItem.contains("Reports")) {
                showReports();

            }
            else if (menuItem.contains("Course Assignments")) {
                showCourseAssignments();
            }
            else if(menuItem.contains("Backup & RestorE")){
                showBackupRestore();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // DASHBOARD
    private void showDashboard() {
        try {
            JPanel dashboard = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel welcomeLabel = new JLabel("System Overview");
            welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            dashboard.add(welcomeLabel, gbc);

            Map<String, Integer> stats = adminService.getSystemStats();

            gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            dashboard.add(createStatCard("Total Students", String.valueOf(stats.get("total_students")), new Color(52, 152, 219)), gbc);
            gbc.gridx = 1;
            dashboard.add(createStatCard("Total Instructors", String.valueOf(stats.get("total_instructors")), new Color(46, 204, 113)), gbc);

            gbc.gridy = 2;
            gbc.gridx = 0;
            dashboard.add(createStatCard("Total Courses", String.valueOf(stats.get("total_courses")), new Color(155, 89, 182)), gbc);
            gbc.gridx = 1;
            dashboard.add(createStatCard("Total Enrollments", String.valueOf(stats.get("total_enrollments")), new Color(230, 126, 34)), gbc);

            mainPanel.add(dashboard, BorderLayout.CENTER);
        } catch (Exception e) {
            mainPanel.add(new JLabel("Error loading dashboard: " + e.getMessage()));
        }
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        card.setPreferredSize(new Dimension(250, 150));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 48));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // USER MANAGEMENT
    private void showUserManagement() {
        try {
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel titleLabel = new JLabel("User Management");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JButton addStudentBtn = new JButton("➕ Add Student");
            JButton addInstructorBtn = new JButton("➕ Add Instructor");
            JButton addAdminBtn = new JButton("➕ Add Admin");
            JButton refreshButton = new JButton("🔄 Refresh");

            addStudentBtn.addActionListener(e -> showAddStudentDialog());
            addInstructorBtn.addActionListener(e -> showAddInstructorDialog());
            addAdminBtn.addActionListener(e -> showAddAdminDialog());
            refreshButton.addActionListener(e -> handleNavigation("👥 User Management"));

            buttonPanel.add(addStudentBtn);
            buttonPanel.add(addInstructorBtn);
            buttonPanel.add(addAdminBtn);
            buttonPanel.add(refreshButton);

            List<Map<String, String>> users = adminService.getAllUsers();
            String[] columns = {"User ID", "Username", "Role", "Created At"};
            Object[][] data = new Object[users.size()][4];

            for (int i = 0; i < users.size(); i++) {
                Map<String, String> user = users.get(i);
                data[i][0] = user.get("user_id");
                data[i][1] = user.get("username");
                data[i][2] = user.get("role");
                data[i][3] = user.get("created_at");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(buttonPanel, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(panel);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
        }
    }
    // ADD USER DIALOGS

//    private void showAddStudentDialog() {
//        JDialog dialog = new JDialog(this, "Add New Student", true);
//        dialog.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(10, 10, 10, 10);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        JTextField usernameField = new JTextField(20);
//        JPasswordField passwordField = new JPasswordField(20);
//        JTextField rollNumberField = new JTextField(20);
//        JTextField firstNameField = new JTextField(20);
//        JTextField lastNameField = new JTextField(20);
//        JTextField emailField = new JTextField(20);
//        JTextField programField = new JTextField(20);
//        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
//
//        int row = 0;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Username:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(usernameField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Password:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(passwordField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Roll Number:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(rollNumberField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("First Name:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(firstNameField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Last Name:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(lastNameField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Email:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(emailField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Program:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(programField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Year:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(yearSpinner, gbc);
//        row++;
//
//        JButton createButton = new JButton("Create Student");
//        createButton.addActionListener(e -> {
//            try {
//                adminService.createCompleteStudent(
//                        usernameField.getText(),
//                        new String(passwordField.getPassword()),
//                        rollNumberField.getText(),
//                        firstNameField.getText(),
//                        lastNameField.getText(),
//                        emailField.getText(),
//                        programField.getText(),
//                        (Integer) yearSpinner.getValue()
//                );
//                JOptionPane.showMessageDialog(dialog, "Student created successfully!");
//                dialog.dispose();
//                handleNavigation("🎓 Student Management");
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//
//        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
//        dialog.add(createButton, gbc);
//
//        dialog.pack();
//        dialog.setLocationRelativeTo(this);
//        dialog.setVisible(true);
//    }
private void showAddStudentDialog() {
    JDialog dialog = new JDialog(this, "Add New Student", true);
    dialog.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JTextField usernameField = new JTextField(20);
    JPasswordField passwordField = new JPasswordField(20);
    JTextField rollNumberField = new JTextField(20);
    JTextField firstNameField = new JTextField(20);
    JTextField lastNameField = new JTextField(20);
    JTextField emailField = new JTextField(20);
    JTextField phoneField = new JTextField(20);  // NEW
    JTextField programField = new JTextField(20);
    JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));

    int row = 0;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Username:"), gbc);
    gbc.gridx = 1;
    dialog.add(usernameField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Password:"), gbc);
    gbc.gridx = 1;
    dialog.add(passwordField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Roll Number:"), gbc);
    gbc.gridx = 1;
    dialog.add(rollNumberField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("First Name:"), gbc);
    gbc.gridx = 1;
    dialog.add(firstNameField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Last Name:"), gbc);
    gbc.gridx = 1;
    dialog.add(lastNameField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Email:"), gbc);
    gbc.gridx = 1;
    dialog.add(emailField, gbc);
    row++;

    // NEW: Phone field
    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Phone Number:"), gbc);
    gbc.gridx = 1;
    dialog.add(phoneField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Program:"), gbc);
    gbc.gridx = 1;
    dialog.add(programField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Year:"), gbc);
    gbc.gridx = 1;
    dialog.add(yearSpinner, gbc);
    row++;

    JButton createButton = new JButton("Create Student");
    createButton.addActionListener(e -> {
        try {
            // Validate phone number
            String phone = phoneField.getText().trim();
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Phone number is required");
                return;
            }
            if (!phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(dialog, "Phone number must be 10 digits");
                return;
            }

            adminService.createCompleteStudent(
                    usernameField.getText(),
                    new String(passwordField.getPassword()),
                    rollNumberField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    programField.getText(),
                    (Integer) yearSpinner.getValue(),
                    phone  // NEW parameter
            );
            JOptionPane.showMessageDialog(dialog, "Student created successfully!");
            dialog.dispose();
            handleNavigation("🎓 Student Management");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    });

    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
    dialog.add(createButton, gbc);

    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}


//    private void showAddInstructorDialog() {
//        JDialog dialog = new JDialog(this, "Add New Instructor", true);
//        dialog.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(10, 10, 10, 10);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        JTextField usernameField = new JTextField(20);
//        JPasswordField passwordField = new JPasswordField(20);
//        JTextField employeeIdField = new JTextField(20);
//        JTextField firstNameField = new JTextField(20);
//        JTextField lastNameField = new JTextField(20);
//        JTextField emailField = new JTextField(20);
//        JTextField departmentField = new JTextField(20);
//
//        int row = 0;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Username:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(usernameField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Password:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(passwordField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Employee ID:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(employeeIdField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("First Name:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(firstNameField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Last Name:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(lastNameField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Email:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(emailField, gbc);
//        row++;
//
//        gbc.gridx = 0; gbc.gridy = row;
//        dialog.add(new JLabel("Department:"), gbc);
//        gbc.gridx = 1;
//        dialog.add(departmentField, gbc);
//        row++;
//
//        JButton createButton = new JButton("Create Instructor");
//        createButton.addActionListener(e -> {
//            try {
//                adminService.createCompleteInstructor(
//                        usernameField.getText(),
//                        new String(passwordField.getPassword()),
//                        employeeIdField.getText(),
//                        firstNameField.getText(),
//                        lastNameField.getText(),
//                        emailField.getText(),
//                        departmentField.getText()
//                );
//                JOptionPane.showMessageDialog(dialog, "Instructor created successfully!");
//                dialog.dispose();
//                handleNavigation("👨‍🏫 Instructor Management");
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//
//        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
//        dialog.add(createButton, gbc);
//
//        dialog.pack();
//        dialog.setLocationRelativeTo(this);
//        dialog.setVisible(true);
//    }
private void showAddInstructorDialog() {
    JDialog dialog = new JDialog(this, "Add New Instructor", true);
    dialog.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JTextField usernameField = new JTextField(20);
    JPasswordField passwordField = new JPasswordField(20);
    JTextField employeeIdField = new JTextField(20);
    JTextField firstNameField = new JTextField(20);
    JTextField lastNameField = new JTextField(20);
    JTextField emailField = new JTextField(20);
    JTextField phoneField = new JTextField(20);  // NEW
    JTextField departmentField = new JTextField(20);

    int row = 0;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Username:"), gbc);
    gbc.gridx = 1;
    dialog.add(usernameField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Password:"), gbc);
    gbc.gridx = 1;
    dialog.add(passwordField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Employee ID:"), gbc);
    gbc.gridx = 1;
    dialog.add(employeeIdField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("First Name:"), gbc);
    gbc.gridx = 1;
    dialog.add(firstNameField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Last Name:"), gbc);
    gbc.gridx = 1;
    dialog.add(lastNameField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Email:"), gbc);
    gbc.gridx = 1;
    dialog.add(emailField, gbc);
    row++;

    // NEW: Phone field
    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Phone Number:"), gbc);
    gbc.gridx = 1;
    dialog.add(phoneField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    dialog.add(new JLabel("Department:"), gbc);
    gbc.gridx = 1;
    dialog.add(departmentField, gbc);
    row++;

    JButton createButton = new JButton("Create Instructor");
    createButton.addActionListener(e -> {
        try {
            // Validate phone number
            String phone = phoneField.getText().trim();
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Phone number is required");
                return;
            }
            if (!phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(dialog, "Phone number must be 10 digits");
                return;
            }

            adminService.createCompleteInstructor(
                    usernameField.getText(),
                    new String(passwordField.getPassword()),
                    employeeIdField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    departmentField.getText(),
                    phone  // NEW parameter
            );
            JOptionPane.showMessageDialog(dialog, "Instructor created successfully!");
            dialog.dispose();
            handleNavigation("👨‍🏫 Instructor Management");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    });

    gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
    dialog.add(createButton, gbc);

    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}


    private void showAddAdminDialog() {
        JDialog dialog = new JDialog(this, "Add New Admin", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        dialog.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        JButton createButton = new JButton("Create Admin");
        createButton.addActionListener(e -> {
            try {
                adminService.createAdmin(
                        usernameField.getText(),
                        new String(passwordField.getPassword())
                );
                JOptionPane.showMessageDialog(dialog, "Admin created successfully!");
                dialog.dispose();
                handleNavigation("👥 User Management");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        dialog.add(createButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // STUDENT MANAGEMENT
    private void showStudentManagement() {
        try {
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel titleLabel = new JLabel("Student Management");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addButton = new JButton("➕ Add Student");
            JButton refreshButton = new JButton("🔄 Refresh");

            addButton.addActionListener(e -> showAddStudentDialog());
            refreshButton.addActionListener(e -> handleNavigation("🎓 Student Management"));

            buttonPanel.add(addButton);
            buttonPanel.add(refreshButton);

            List<Map<String, String>> students = adminService.getAllStudents();
            String[] columns = {"ID", "Roll No", "Name", "Email", "Program", "Year", "Status"};
            Object[][] data = new Object[students.size()][7];

            for (int i = 0; i < students.size(); i++) {
                Map<String, String> student = students.get(i);
                data[i][0] = student.get("student_id");
                data[i][1] = student.get("roll_number");
                data[i][2] = student.get("first_name") + " " + student.get("last_name");
                data[i][3] = student.get("email");
                data[i][4] = student.get("program");
                data[i][5] = student.get("year_of_study");
                data[i][6] = student.get("status");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(buttonPanel, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(panel);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
        }
    }

    // INSTRUCTOR MANAGEMENT
    private void showInstructorManagement() {
        try {
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel titleLabel = new JLabel("Instructor Management");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addButton = new JButton("➕ Add Instructor");
            JButton refreshButton = new JButton("🔄 Refresh");

            addButton.addActionListener(e -> showAddInstructorDialog());
            refreshButton.addActionListener(e -> handleNavigation("👨‍🏫 Instructor Management"));

            buttonPanel.add(addButton);
            buttonPanel.add(refreshButton);

            List<Map<String, String>> instructors = adminService.getAllInstructors();
            String[] columns = {"ID", "Employee ID", "Name", "Email", "Department", "Status"};
            Object[][] data = new Object[instructors.size()][6];

            for (int i = 0; i < instructors.size(); i++) {
                Map<String, String> instructor = instructors.get(i);
                data[i][0] = instructor.get("instructor_id");
                data[i][1] = instructor.get("employee_id");
                data[i][2] = instructor.get("first_name") + " " + instructor.get("last_name");
                data[i][3] = instructor.get("email");
                data[i][4] = instructor.get("department");
                data[i][5] = instructor.get("status");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(buttonPanel, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(panel);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
        }
    }

    // COURSE MANAGEMENT
    private void showCourseManagement() {
        try {
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel titleLabel = new JLabel("Course Management");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addButton = new JButton("➕ Add Course");
            JButton refreshButton = new JButton("🔄 Refresh");
            JCheckBox maintenanceModeSwitch = new JCheckBox("Maintenance Mode");
            try {
                maintenanceModeSwitch.setSelected(adminService.isMaintenanceModeOn());
            } catch (Exception ex) {
                maintenanceModeSwitch.setSelected(false); // fallback: default off
            }

// Add an ActionListener—not ItemListener!
            maintenanceModeSwitch.addActionListener(e -> {
                boolean enabled = maintenanceModeSwitch.isSelected();
                try {
                    adminService.setMaintenanceMode(enabled);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating maintenance mode: " + ex.getMessage());
                }
                JOptionPane.showMessageDialog(this, enabled
                        ? "Maintenance Mode enabled! Users can't add/drop courses or edit marks."
                        : "Maintenance Mode disabled. Functionality restored.");
            });

            addButton.addActionListener(e -> showAddCourseDialog());
            refreshButton.addActionListener(e -> handleNavigation("📚 Course Management"));

            buttonPanel.add(addButton);
            buttonPanel.add(refreshButton);
            buttonPanel.add(maintenanceModeSwitch);

            List<Map<String, String>> courses = adminService.getAllCourses();
            String[] columns = {"ID", "Code", "Title", "Credits", "Department", "Active"};
            Object[][] data = new Object[courses.size()][6];

            for (int i = 0; i < courses.size(); i++) {
                Map<String, String> course = courses.get(i);
                data[i][0] = course.get("course_id");
                data[i][1] = course.get("course_code");
                data[i][2] = course.get("title");
                data[i][3] = course.get("credits");
                data[i][4] = course.get("department");
                data[i][5] = course.get("is_active");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(buttonPanel, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(panel);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
        }
    }

    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Add New Course", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField codeField = new JTextField(20);
        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JTextField creditsField = new JTextField(20);
        JTextField deptField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        dialog.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        dialog.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        dialog.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1;
        dialog.add(creditsField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        dialog.add(deptField, gbc);

        JButton createButton = new JButton("Create Course");
        createButton.addActionListener(e -> {
            try {
                adminService.addCourse(codeField.getText(), titleField.getText(),
                        descArea.getText(), Integer.parseInt(creditsField.getText()),
                        deptField.getText());
                JOptionPane.showMessageDialog(dialog, "Course created successfully!");
                dialog.dispose();
                handleNavigation("📚 Course Management");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        dialog.add(createButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // TIMETABLE/SECTIONS MANAGEMENT
    private void showTimetableManagement() {
        try {
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel titleLabel = new JLabel("Timetable & Sections");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton refreshButton = new JButton("🔄 Refresh");

            refreshButton.addActionListener(e -> handleNavigation("📅 Timetable/Sections"));
            buttonPanel.add(refreshButton);

            List<Map<String, String>> sections = adminService.getAllSections();
            String[] columns = {"ID", "Course", "Section", "Instructor", "Semester", "Day", "Time", "Capacity", "Enrolled"};
            Object[][] data = new Object[sections.size()][9];

            for (int i = 0; i < sections.size(); i++) {
                Map<String, String> section = sections.get(i);
                data[i][0] = section.get("section_id");
                data[i][1] = section.get("course_code");
                data[i][2] = section.get("section_number");
                data[i][3] = section.get("instructor");
                data[i][4] = section.get("semester") + " " + section.get("year");
                data[i][5] = section.get("schedule_day");
                data[i][6] = section.get("schedule_time");
                data[i][7] = section.get("capacity");
                data[i][8] = section.get("enrolled");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            JScrollPane scrollPane = new JScrollPane(table);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(buttonPanel, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(panel);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
        }
    }

//    // BACKUP & RESTORE
//    private void showBackupRestore() {
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(20, 20, 20, 20);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        JLabel titleLabel = new JLabel("Database Backup & Restore");
//        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
//        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
//        panel.add(titleLabel, gbc);
//
//        JButton backupButton = new JButton("💾 Create Backup");
//        backupButton.setPreferredSize(new Dimension(200, 50));
//        backupButton.addActionListener(e -> performBackup());
//
//        JButton restoreButton = new JButton("♻️ Restore from Backup");
//        restoreButton.setPreferredSize(new Dimension(200, 50));
//        restoreButton.addActionListener(e -> performRestore());
//
//        gbc.gridy = 1; gbc.gridwidth = 1;
//        gbc.gridx = 0;
//        panel.add(backupButton, gbc);
//        gbc.gridx = 1;
//        panel.add(restoreButton, gbc);
//
//        JTextArea infoArea = new JTextArea(10, 40);
//        infoArea.setText("Backup Information:\n\n" +
//                "- Backups include both university_auth and university_erp databases\n" +
//                "- Backup files are saved with timestamp\n" +
//                "- Restore will overwrite current database\n" +
//                "- Always test backups in a safe environment first\n\n" +
//                "Backup Location: Choose directory when creating backup");
//        infoArea.setEditable(false);
//        infoArea.setBorder(BorderFactory.createTitledBorder("Information"));
//
//        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
//        panel.add(new JScrollPane(infoArea), gbc);
//
//        mainPanel.add(panel, BorderLayout.CENTER);
//    }
//
//    private void performBackup() {
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        fileChooser.setDialogTitle("Select Backup Directory");
//
//        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//            String backupDir = fileChooser.getSelectedFile().getAbsolutePath();
//            SwingWorker<String, Void> worker = new SwingWorker<>() {
//                @Override
//                protected String doInBackground() throws Exception {
//                    return adminService.backupDatabase(backupDir);
//                }
//
//                @Override
//                protected void done() {
//                    try {
//                        String backupFile = get();
//                        JOptionPane.showMessageDialog(admindashboard.this,
//                                "Backup created successfully!\nFile: " + backupFile,
//                                "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
//                    } catch (Exception e) {
//                        JOptionPane.showMessageDialog(admindashboard.this,
//                                "Backup failed: " + e.getMessage(),
//                                "Error", JOptionPane.ERROR_MESSAGE);
//                    }
//                }
//            };
//            worker.execute();
//        }
//    }

    private void performRestore() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will overwrite the current database. Continue?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Backup File");

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String backupFile = fileChooser.getSelectedFile().getAbsolutePath();
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        adminService.restoreDatabase(backupFile);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            JOptionPane.showMessageDialog(admindashboard.this,
                                    "Database restored successfully!",
                                    "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(admindashboard.this,
                                    "Restore failed: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        }
    }
//    private void showCourseAssignments() {
//        try {
//            JPanel panel = new JPanel(new BorderLayout(10, 10));
//
//
//
//
//            JLabel titleLabel = new JLabel("Course-Instructor Assignments");
//            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
//            panel.add(titleLabel, BorderLayout.NORTH);
//
//            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//            JButton assignButton = new JButton("➕ Create New Section");
//            JButton editButton = new JButton("✏️ Change Instructor");
//            JButton refreshButton = new JButton("🔄 Refresh");
//
//
//
//            JButton deleteButton = new JButton("🗑️ Delete Section");
//            deleteButton.setBackground(new Color(231, 76, 60));
//            deleteButton.setForeground(Color.WHITE);
//            deleteButton.setFocusPainted(false);
//
//
//
//
//            assignButton.addActionListener(e -> showAssignInstructorDialog());
//            editButton.addActionListener(e -> showEditAssignmentDialog());
//            refreshButton.addActionListener(e -> handleNavigation("🔗 Course Assignments"));
//
//            buttonPanel.add(assignButton);
//            buttonPanel.add(editButton);
//            buttonPanel.add(refreshButton);
//
//            List<Map<String, String>> assignments = adminService.getCourseInstructorAssignments();
//            String[] columns = {"Section ID", "Course", "Section", "Instructor", "Semester", "Schedule", "Enrollment"};
//            Object[][] data = new Object[assignments.size()][7];
//
//            for (int i = 0; i < assignments.size(); i++) {
//                Map<String, String> assignment = assignments.get(i);
//                data[i][0] = assignment.get("section_id");
//                data[i][1] = assignment.get("course_code") + " - " + assignment.get("course_title");
//                data[i][2] = assignment.get("section_number");
//                data[i][3] = assignment.get("instructor_name");
//                data[i][4] = assignment.get("semester") + " " + assignment.get("year");
//                data[i][5] = assignment.get("schedule");
//                data[i][6] = assignment.get("enrollment");
//            }
//
//            JTable table = new JTable(data, columns);
//            table.setRowHeight(25);
//            table.getColumnModel().getColumn(1).setPreferredWidth(250);
//            JScrollPane scrollPane = new JScrollPane(table);
//
//            JPanel centerPanel = new JPanel(new BorderLayout());
//            centerPanel.add(buttonPanel, BorderLayout.NORTH);
//            centerPanel.add(scrollPane, BorderLayout.CENTER);
//
//            panel.add(centerPanel, BorderLayout.CENTER);
//            mainPanel.add(panel);
//
//        } catch (Exception e) {
//            mainPanel.add(new JLabel("Error: " + e.getMessage()));
//            e.printStackTrace();
//        }
//    }

    private void showCourseAssignments() {
        try {
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel titleLabel = new JLabel("Course-Instructor Assignments");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton assignButton = new JButton("➕ Create New Section");
            JButton editButton = new JButton("✏️ Change Instructor");
            JButton refreshButton = new JButton("🔄 Refresh");
            JButton deleteButton = new JButton("🗑️ Delete Section");

            deleteButton.setBackground(new Color(231, 76, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);

            assignButton.addActionListener(e -> showAssignInstructorDialog());
            editButton.addActionListener(e -> showEditAssignmentDialog());
            refreshButton.addActionListener(e -> handleNavigation("🔗 Course Assignments"));

            buttonPanel.add(assignButton);
            buttonPanel.add(editButton);
            buttonPanel.add(refreshButton);
            buttonPanel.add(deleteButton);

            List<Map<String, String>> assignments = adminService.getCourseInstructorAssignments();
            String[] columns = {"Section ID", "Course", "Section", "Instructor", "Semester", "Schedule", "Enrollment"};
            Object[][] data = new Object[assignments.size()][7];

            for (int i = 0; i < assignments.size(); i++) {
                Map<String, String> assignment = assignments.get(i);
                data[i][0] = assignment.get("section_id");
                data[i][1] = assignment.get("course_code") + " - " + assignment.get("course_title");
                data[i][2] = assignment.get("section_number");
                data[i][3] = assignment.get("instructor_name");
                data[i][4] = assignment.get("semester") + " " + assignment.get("year");
                data[i][5] = assignment.get("schedule");
                data[i][6] = assignment.get("enrollment");
            }

            final JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            table.getColumnModel().getColumn(1).setPreferredWidth(250);

            deleteButton.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this,
                            "Please select a section to delete.",
                            "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int sectionId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
                String courseName = table.getValueAt(selectedRow, 1).toString();
                String sectionNum = table.getValueAt(selectedRow, 2).toString();

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this section?\n\n" +
                                "Course: " + courseName + "\n" +
                                "Section: " + sectionNum + "\n\n" +
                                "This action cannot be undone!",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        adminService.deleteSection(sectionId);
                        JOptionPane.showMessageDialog(this,
                                "Section deleted successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        handleNavigation("🔗 Course Assignments"); // Refresh
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this,
                                "Error deleting section: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.add(buttonPanel, BorderLayout.NORTH);
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            panel.add(centerPanel, BorderLayout.CENTER);
            mainPanel.add(panel);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }


    private void showAssignInstructorDialog() {
        JDialog dialog = new JDialog(this, "Assign Instructor to Course", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try {
            // Get courses and instructors
            List<Map<String, String>> courses = adminService.getCoursesForDropdown();
            List<Map<String, String>> instructors = adminService.getInstructorsForDropdown();

            // Create course dropdown
            String[] courseOptions = courses.stream()
                    .map(c -> c.get("display"))
                    .toArray(String[]::new);
            JComboBox<String> courseCombo = new JComboBox<>(courseOptions);

            // Create instructor dropdown
            String[] instructorOptions = instructors.stream()
                    .map(i -> i.get("display"))
                    .toArray(String[]::new);
            JComboBox<String> instructorCombo = new JComboBox<>(instructorOptions);

            // Other fields
            JTextField sectionField = new JTextField(20);
            JComboBox<String> semesterCombo = new JComboBox<>(new String[]{"Fall", "Spring", "Summer"});
            JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2025, 2024, 2030, 1));
            JTextField dayField = new JTextField(20);
            dayField.setText("Mon/Wed");
            JTextField timeField = new JTextField(20);
            timeField.setText("10:00:00");
            JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(40, 10, 200, 5));
            JTextField deadlineField = new JTextField(20);
            deadlineField.setText("2025-12-31");

            int row = 0;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Course:"), gbc);
            gbc.gridx = 1;
            dialog.add(courseCombo, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Instructor:"), gbc);
            gbc.gridx = 1;
            dialog.add(instructorCombo, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Section Number:"), gbc);
            gbc.gridx = 1;
            dialog.add(sectionField, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Semester:"), gbc);
            gbc.gridx = 1;
            dialog.add(semesterCombo, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Year:"), gbc);
            gbc.gridx = 1;
            dialog.add(yearSpinner, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Schedule Day:"), gbc);
            gbc.gridx = 1;
            dialog.add(dayField, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Schedule Time:"), gbc);
            gbc.gridx = 1;
            dialog.add(timeField, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Capacity:"), gbc);
            gbc.gridx = 1;
            dialog.add(capacitySpinner, gbc);
            row++;

            gbc.gridx = 0; gbc.gridy = row;
            dialog.add(new JLabel("Reg. Deadline (YYYY-MM-DD):"), gbc);
            gbc.gridx = 1;
            dialog.add(deadlineField, gbc);
            row++;

            JButton createButton = new JButton("Create Section");
            createButton.addActionListener(e -> {
                try {
                    int courseIndex = courseCombo.getSelectedIndex();
                    int instructorIndex = instructorCombo.getSelectedIndex();

                    int courseId = Integer.parseInt(courses.get(courseIndex).get("course_id"));
                    int instructorId = Integer.parseInt(instructors.get(instructorIndex).get("instructor_id"));

                    adminService.assignInstructorToCourse(
                            courseId,
                            instructorId,
                            sectionField.getText(),
                            (String) semesterCombo.getSelectedItem(),
                            (Integer) yearSpinner.getValue(),
                            dayField.getText(),
                            timeField.getText(),
                            (Integer) capacitySpinner.getValue(),
                            deadlineField.getText()
                    );

                    JOptionPane.showMessageDialog(dialog, "Section created successfully!");
                    dialog.dispose();
                    handleNavigation("🔗 Course Assignments");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
            dialog.add(createButton, gbc);

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void showEditAssignmentDialog() {
        JDialog dialog = new JDialog(this, "Change Instructor for Section", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try {
            List<Map<String, String>> instructors = adminService.getInstructorsForDropdown();

            JTextField sectionIdField = new JTextField(20);

            String[] instructorOptions = instructors.stream()
                    .map(i -> i.get("display"))
                    .toArray(String[]::new);
            JComboBox<String> instructorCombo = new JComboBox<>(instructorOptions);

            gbc.gridx = 0; gbc.gridy = 0;
            dialog.add(new JLabel("Section ID:"), gbc);
            gbc.gridx = 1;
            dialog.add(sectionIdField, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            dialog.add(new JLabel("New Instructor:"), gbc);
            gbc.gridx = 1;
            dialog.add(instructorCombo, gbc);

            JButton updateButton = new JButton("Update Assignment");
            updateButton.addActionListener(e -> {
                try {
                    int sectionId = Integer.parseInt(sectionIdField.getText());
                    int instructorIndex = instructorCombo.getSelectedIndex();
                    int instructorId = Integer.parseInt(instructors.get(instructorIndex).get("instructor_id"));

                    adminService.updateSectionInstructor(sectionId, instructorId);
                    JOptionPane.showMessageDialog(dialog, "Instructor updated successfully!");
                    dialog.dispose();
                    handleNavigation("🔗 Course Assignments");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            dialog.add(updateButton, gbc);

            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    // REPORTS
    private void showReports() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton enrollmentReport = new JButton("📊 Enrollment Report");
        JButton gradeReport = new JButton("📈 Grade Distribution");
        JButton courseReport = new JButton("📚 Course Analytics");
        JButton studentReport = new JButton("🎓 Student Statistics");
        JButton instructorReport = new JButton("👨‍🏫 Instructor Workload");
        JButton financialReport = new JButton("💰 System Overview");

        enrollmentReport.setPreferredSize(new Dimension(200, 80));
        gradeReport.setPreferredSize(new Dimension(200, 80));
        courseReport.setPreferredSize(new Dimension(200, 80));
        studentReport.setPreferredSize(new Dimension(200, 80));
        instructorReport.setPreferredSize(new Dimension(200, 80));
        financialReport.setPreferredSize(new Dimension(200, 80));

        panel.add(enrollmentReport);
        panel.add(gradeReport);
        panel.add(courseReport);
        panel.add(studentReport);
        panel.add(instructorReport);
        panel.add(financialReport);

        mainPanel.add(panel, BorderLayout.CENTER);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new loginframe().setVisible(true));
        }
    }



    private void showBackupRestore() {
        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("💾 Backup & Restore", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Backup Section
        JLabel backupLabel = new JLabel("Create Database Backup");
        backupLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(backupLabel, gbc);

        JLabel backupDirLabel = new JLabel("Backup Directory:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        centerPanel.add(backupDirLabel, gbc);

        JTextField backupDirField = new JTextField(System.getProperty("user.home") + "/erp_backups", 30);
        gbc.gridx = 1; gbc.gridy = 1;
        centerPanel.add(backupDirField, gbc);

        JButton browseBackupButton = new JButton("Browse...");
        browseBackupButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                backupDirField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        gbc.gridx = 2; gbc.gridy = 1;
        centerPanel.add(browseBackupButton, gbc);

        JButton createBackupButton = new JButton("🗄️ Create Backup");
        createBackupButton.setBackground(new Color(52, 152, 219));
        createBackupButton.setForeground(Color.WHITE);
        createBackupButton.setFocusPainted(false);
        createBackupButton.addActionListener(e -> {
            String backupDir = backupDirField.getText().trim();
            if (backupDir.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please specify a backup directory.");
                return;
            }

            // Create directory if it doesn't exist
            new File(backupDir).mkdirs();

            try {
                String result = adminService.createDatabaseBackup(backupDir);
                JOptionPane.showMessageDialog(this, result, "Backup Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Backup failed: " + ex.getMessage() +
                                "\n\nNote: Make sure mysqldump is available in your PATH.",
                        "Backup Error",
                        JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        centerPanel.add(createBackupButton, gbc);

        // Separator
        JSeparator separator = new JSeparator();
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 10, 20, 10);
        centerPanel.add(separator, gbc);
        gbc.insets = new Insets(10, 10, 10, 10);

        // Restore Section
        JLabel restoreLabel = new JLabel("Restore Database from Backup");
        restoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        centerPanel.add(restoreLabel, gbc);

        JLabel authFileLabel = new JLabel("Auth DB Backup File:");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        centerPanel.add(authFileLabel, gbc);

        JTextField authFileField = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 5;
        centerPanel.add(authFileField, gbc);

        JButton browseAuthButton = new JButton("Browse...");
        browseAuthButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(backupDirField.getText());
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Files", "sql"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                authFileField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        gbc.gridx = 2; gbc.gridy = 5;
        centerPanel.add(browseAuthButton, gbc);

        JLabel erpFileLabel = new JLabel("ERP DB Backup File:");
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        centerPanel.add(erpFileLabel, gbc);

        JTextField erpFileField = new JTextField(30);
        gbc.gridx = 1; gbc.gridy = 6;
        centerPanel.add(erpFileField, gbc);

        JButton browseErpButton = new JButton("Browse...");
        browseErpButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(backupDirField.getText());
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Files", "sql"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                erpFileField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        gbc.gridx = 2; gbc.gridy = 6;
        centerPanel.add(browseErpButton, gbc);

        JButton restoreButton = new JButton("⚠️ Restore Database");
        restoreButton.setBackground(new Color(231, 76, 60));
        restoreButton.setForeground(Color.WHITE);
        restoreButton.setFocusPainted(false);
        restoreButton.addActionListener(e -> {
            String authFile = authFileField.getText().trim();
            String erpFile = erpFileField.getText().trim();

            if (authFile.isEmpty() || erpFile.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select both backup files.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "⚠️ WARNING: This will replace ALL current data with the backup!\n\n" +
                            "Are you absolutely sure you want to continue?",
                    "Confirm Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    String result = adminService.restoreDatabaseBackup(authFile, erpFile);
                    JOptionPane.showMessageDialog(this, result, "Restore Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Restore failed: " + ex.getMessage() +
                                    "\n\nNote: Make sure mysql client is available in your PATH.",
                            "Restore Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        centerPanel.add(restoreButton, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }





    // COURSE-INSTRUCTOR ASSIGNMENTS


}
