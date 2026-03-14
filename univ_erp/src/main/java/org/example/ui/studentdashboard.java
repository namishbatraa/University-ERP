package org.example.ui;

import org.example.service.StudentService;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class studentdashboard extends JFrame {
    private final String username;
    private final StudentService studentService;
    private int studentId;
    private JPanel mainPanel;
    private Map<String, String> studentProfile;

    public studentdashboard(String username) {
        super("Student Dashboard - " + username);
        this.username = username;
        this.studentService = new StudentService();

        // Load student data
        try {
            this.studentId = studentService.getStudentIdByUsername(username);
            this.studentProfile = studentService.getStudentProfile(studentId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage());
            dispose();
            return;
        }

        initUI();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.WEST);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        showWelcomeScreen();
        add(mainPanel, BorderLayout.CENTER);
    }
    private void showNotificationsWindow() {
        JDialog notifDialog = new JDialog(this, "📬 Notifications", false);
        notifDialog.setLayout(new BorderLayout(10, 10));
        notifDialog.setSize(500, 600);
        notifDialog.setLocationRelativeTo(this);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel headerLabel = new JLabel("Your Notifications");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);

        JButton markAllReadBtn = new JButton("Mark All Read");
        markAllReadBtn.setForeground(Color.WHITE);
        markAllReadBtn.setBackground(new Color(46, 204, 113));
        markAllReadBtn.setFocusPainted(false);
        markAllReadBtn.addActionListener(e -> {
            try {
                studentService.markAllAsRead(studentId);
                notifDialog.dispose();
                showNotificationsWindow(); // Refresh
                // Update badge
                handleNavigation("📊 Dashboard"); // Refresh dashboard to update badge
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(notifDialog, "Error: " + ex.getMessage());
            }
        });

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(markAllReadBtn, BorderLayout.EAST);

        // Notification list
        JPanel notificationsPanel = new JPanel();
        notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.setBackground(Color.WHITE);

        try {
            List<Map<String, String>> notifications = studentService.getNotifications(studentId, false);

            if (notifications.isEmpty()) {
                JLabel noNotifLabel = new JLabel("No notifications", SwingConstants.CENTER);
                noNotifLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                noNotifLabel.setForeground(Color.GRAY);
                notificationsPanel.add(noNotifLabel);
            } else {
                for (Map<String, String> notif : notifications) {
                    JPanel notifCard = createNotificationCard(notif, notifDialog);
                    notificationsPanel.add(notifCard);
                    notificationsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Error loading notifications: " + e.getMessage());
            errorLabel.setForeground(Color.RED);
            notificationsPanel.add(errorLabel);
        }

        JScrollPane scrollPane = new JScrollPane(notificationsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        notifDialog.add(headerPanel, BorderLayout.NORTH);
        notifDialog.add(scrollPane, BorderLayout.CENTER);
        notifDialog.setVisible(true);
    }

    private JPanel createNotificationCard(Map<String, String> notif, JDialog parentDialog) {
        boolean isRead = Boolean.parseBoolean(notif.get("is_read"));

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isRead ? Color.LIGHT_GRAY : new Color(52, 152, 219), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(isRead ? Color.WHITE : new Color(232, 245, 253));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Icon based on type
        String icon = "📢";
        String type = notif.get("type");
        if (type.equals("GRADE_POSTED")) {
            icon = "📝";
        } else if (type.equals("COURSE_ADDED")) {
            icon = "📚";
        } else if (type.equals("ANNOUNCEMENT")) {
            icon = "📣";
        }

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 32));

        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(notif.get("title"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JTextArea messageArea = new JTextArea(notif.get("message"));
        messageArea.setFont(new Font("Arial", Font.PLAIN, 12));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setRows(2);

        JLabel timeLabel = new JLabel(notif.get("created_at"));
        timeLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        timeLabel.setForeground(Color.GRAY);

        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(messageArea, BorderLayout.CENTER);
        contentPanel.add(timeLabel, BorderLayout.SOUTH);

        // Mark as read button
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setOpaque(false);

        if (!isRead) {
            JButton markReadBtn = new JButton("✓");
            markReadBtn.setToolTipText("Mark as read");
            markReadBtn.setFocusPainted(false);
            markReadBtn.addActionListener(e -> {
                try {
                    int notifId = Integer.parseInt(notif.get("notification_id"));
                    studentService.markNotificationAsRead(notifId);
                    parentDialog.dispose();
                    showNotificationsWindow(); // Refresh
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentDialog, "Error: " + ex.getMessage());
                }
            });
            actionPanel.add(markReadBtn, BorderLayout.NORTH);
        }

        card.add(iconLabel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }


    //    private JPanel createHeaderPanel() {
//        JPanel header = new JPanel(new BorderLayout());
//        header.setBackground(new Color(41, 128, 185));
//        header.setPreferredSize(new Dimension(0, 80));
//        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
//
//        String fullName = studentProfile.get("first_name") + " " + studentProfile.get("last_name");
//
//        JLabel titleLabel = new JLabel("📚 Student Portal");
//        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
//        titleLabel.setForeground(Color.WHITE);
//
//        JLabel userLabel = new JLabel("Welcome, " + fullName);
//        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
//        userLabel.setForeground(Color.WHITE);
//
//        JButton logoutButton = new JButton("Logout");
//        logoutButton.addActionListener(e -> logout());
//
//        JPanel leftPanel = new JPanel(new BorderLayout());
//        leftPanel.setOpaque(false);
//        leftPanel.add(titleLabel, BorderLayout.NORTH);
//        leftPanel.add(userLabel, BorderLayout.SOUTH);
//
//        header.add(leftPanel, BorderLayout.WEST);
//        header.add(logoutButton, BorderLayout.EAST);
//
//        return header;
//    }
private JPanel createHeaderPanel() {
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(new Color(41, 128, 185));
    header.setPreferredSize(new Dimension(0, 80));
    header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    String fullName = studentProfile.get("first_name") + " " + studentProfile.get("last_name");

    JLabel titleLabel = new JLabel("📚 Student Portal");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    titleLabel.setForeground(Color.WHITE);

    JLabel userLabel = new JLabel("Welcome, " + fullName);
    userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    userLabel.setForeground(Color.WHITE);

    // Create notification button
    JButton notificationButton = new JButton("🔔");
    notificationButton.setFont(new Font("Arial", Font.PLAIN, 20));
    notificationButton.setFocusPainted(false);
    notificationButton.setBorderPainted(false);
    notificationButton.setContentAreaFilled(false);
    notificationButton.setForeground(Color.WHITE);
    notificationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    notificationButton.addActionListener(e -> showNotificationsWindow());

    // Add badge for unread count
    try {
        int unreadCount = studentService.getUnreadNotificationCount(studentId);
        if (unreadCount > 0) {
            notificationButton.setText("🔔 (" + unreadCount + ")");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    JButton logoutButton = new JButton("Logout");
    logoutButton.addActionListener(e -> logout());

    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setOpaque(false);
    leftPanel.add(titleLabel, BorderLayout.NORTH);
    leftPanel.add(userLabel, BorderLayout.SOUTH);

    // Right panel with notification and logout
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    rightPanel.setOpaque(false);
    rightPanel.add(notificationButton);
    rightPanel.add(logoutButton);

    header.add(leftPanel, BorderLayout.WEST);
    header.add(rightPanel, BorderLayout.EAST);

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
                "➕ Add/Drop Courses",
                "📈 Grades",
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
                btn.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 73, 94));
            }
        });

        return btn;
    }

    private void handleNavigation(String menuItem) {
        mainPanel.removeAll();

        try {
            if (menuItem.contains("Dashboard")) {
                showWelcomeScreen();
            } else if (menuItem.contains("My Courses")) {
                showCoursesPanel();
            } else if (menuItem.contains("Add/Drop")) {
                showAddDropPanel();
            } else if (menuItem.contains("Grades")) {
                showGradesPanel();
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

    // WELCOME SCREEN / DASHBOARD
    private void showWelcomeScreen() {
        try {
            JPanel welcome = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel welcomeLabel = new JLabel("Welcome to your Student Dashboard!");
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
            Map<String, Integer> stats = studentService.getDashboardStats(studentId);

            JPanel statsContainer = new JPanel(new GridLayout(1, 3, 20, 0));
            statsContainer.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

            statsContainer.add(createStatCard("Enrolled Courses",
                    String.valueOf(stats.get("enrolled_courses")), new Color(46, 204, 113)));
            statsContainer.add(createStatCard("Pending Assignments",
                    String.valueOf(stats.get("pending_assignments")), new Color(231, 76, 60)));
            statsContainer.add(createStatCard("Average Grade",
                    stats.get("average_grade") + "%", new Color(52, 152, 219)));

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

    // MY COURSES PANEL
    private void showCoursesPanel() {
        try {
            JLabel label = new JLabel("📚 My Enrolled Courses", SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 18));
            mainPanel.add(label, BorderLayout.NORTH);

            List<Map<String, String>> courses = studentService.getEnrolledCourses(studentId);

            String[] columns = {"Course Code", "Course Name", "Instructor", "Credits", "Schedule"};
            Object[][] data = new Object[courses.size()][5];

            for (int i = 0; i < courses.size(); i++) {
                Map<String, String> course = courses.get(i);
                data[i][0] = course.get("course_code");
                data[i][1] = course.get("title");
                data[i][2] = course.get("instructor");
                data[i][3] = course.get("credits");
                data[i][4] = course.get("schedule");
            }

            JTable table = new JTable(data, columns);
            table.setRowHeight(25);
            table.getColumnModel().getColumn(1).setPreferredWidth(150);
            JScrollPane scrollPane = new JScrollPane(table);
            mainPanel.add(scrollPane, BorderLayout.CENTER);
        } catch (Exception e) {
            mainPanel.add(new JLabel("Error loading courses: " + e.getMessage()));
        }
    }

    // ADD/DROP COURSES PANEL
    private void showAddDropPanel() {
        try {
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            JLabel titleLabel = new JLabel("📝 Course Registration", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            panel.add(titleLabel, BorderLayout.NORTH);

            // Create tabbed pane
            JTabbedPane tabbedPane = new JTabbedPane();

            // ADD COURSES TAB
            JPanel addPanel = createAddCoursesPanel();
            tabbedPane.addTab("📖 Add Courses", addPanel);

            // DROP COURSES TAB
            JPanel dropPanel = createDropCoursesPanel();
            tabbedPane.addTab("📕 Drop Courses", dropPanel);

            panel.add(tabbedPane, BorderLayout.CENTER);
            mainPanel.add(panel);

        } catch (Exception e) {
            mainPanel.add(new JLabel("Error: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private JPanel createAddCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Available Sections - Click Enroll to add course");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);

        try {
            List<Map<String, String>> sections = studentService.getAvailableSections();

            if (sections.isEmpty()) {
                panel.add(new JLabel("No available courses at the moment", SwingConstants.CENTER), BorderLayout.CENTER);
                return panel;
            }

            // Create a panel with GridLayout for courses
            JPanel coursesPanel = new JPanel();
            coursesPanel.setLayout(new BoxLayout(coursesPanel, BoxLayout.Y_AXIS));

            for (Map<String, String> section : sections) {
                JPanel courseCard = new JPanel(new BorderLayout(10, 10));
                courseCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                courseCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
                courseCard.setBackground(new Color(245, 245, 245));

                // Course info
                JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
                infoPanel.setOpaque(false);

                JLabel courseCodeLabel = new JLabel("📌 Course: " + section.get("course_code"));
                courseCodeLabel.setFont(new Font("Arial", Font.BOLD, 12));
                infoPanel.add(courseCodeLabel);

                JLabel sectionLabel = new JLabel("Section: " + section.get("section_number"));
                infoPanel.add(sectionLabel);

                JLabel titleLabel = new JLabel("Title: " + section.get("title"));
                titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                infoPanel.add(titleLabel);

                JLabel instructorLabel = new JLabel("👨‍🏫 Instructor: " + section.get("instructor"));
                instructorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                infoPanel.add(instructorLabel);

                JLabel scheduleLabel = new JLabel("🕐 Schedule: " + section.get("schedule"));
                scheduleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                infoPanel.add(scheduleLabel);

                JLabel seatsLabel = new JLabel("💺 Seats Available: " + section.get("seats_available"));
                seatsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                infoPanel.add(seatsLabel);

                // Enroll button
                JButton enrollButton = new JButton("✓ Enroll");
                enrollButton.setBackground(new Color(46, 204, 113));
                enrollButton.setForeground(Color.WHITE);
                enrollButton.setPreferredSize(new Dimension(100, 40));
                enrollButton.setFont(new Font("Arial", Font.BOLD, 12));

                int sectionId = Integer.parseInt(section.get("section_id"));
                enrollButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Enroll in:\n\n" +
                                    "Course: " + section.get("course_code") + " - " + section.get("title") + "\n" +
                                    "Instructor: " + section.get("instructor") + "\n" +
                                    "Schedule: " + section.get("schedule") + "\n" +
                                    "Section: " + section.get("section_number"),
                            "Confirm Enrollment", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            studentService.enrollInSection(studentId, sectionId);
                            JOptionPane.showMessageDialog(this, "✓ Successfully enrolled in the course!");
                            handleNavigation("➕ Add/Drop Courses");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                courseCard.add(infoPanel, BorderLayout.CENTER);
                courseCard.add(enrollButton, BorderLayout.EAST);

                coursesPanel.add(courseCard);
                coursesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            JScrollPane scrollPane = new JScrollPane(coursesPanel);
            scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            panel.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            panel.add(new JLabel("Error loading sections: " + e.getMessage()), BorderLayout.CENTER);
            e.printStackTrace();
        }

        return panel;
    }

    private JPanel createDropCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("My Enrolled Courses - Click Drop to remove course");
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);

        try {
            List<Map<String, String>> sections = studentService.getEnrolledSectionsWithIds(studentId);

            if (sections.isEmpty()) {
                panel.add(new JLabel("No enrolled courses", SwingConstants.CENTER), BorderLayout.CENTER);
                return panel;
            }

            // Create a panel for enrolled courses
            JPanel coursesPanel = new JPanel();
            coursesPanel.setLayout(new BoxLayout(coursesPanel, BoxLayout.Y_AXIS));

            for (Map<String, String> section : sections) {
                JPanel courseCard = new JPanel(new BorderLayout(10, 10));
                courseCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(231, 76, 60), 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                courseCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
                courseCard.setBackground(new Color(255, 245, 245));

                // Course info
                JPanel infoPanel = new JPanel(new GridLayout(3, 2, 5, 5));
                infoPanel.setOpaque(false);

                JLabel courseCodeLabel = new JLabel("📌 Course: " + section.get("course_code"));
                courseCodeLabel.setFont(new Font("Arial", Font.BOLD, 12));
                infoPanel.add(courseCodeLabel);

                JLabel sectionLabel = new JLabel("Section: " + section.get("section_number"));
                infoPanel.add(sectionLabel);

                JLabel titleLabel = new JLabel("Title: " + section.get("title"));
                titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                infoPanel.add(titleLabel);

                JLabel instructorLabel = new JLabel("👨‍🏫 Instructor: " + section.get("instructor"));
                instructorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                infoPanel.add(instructorLabel);

                JLabel scheduleLabel = new JLabel("🕐 Schedule: " + section.get("schedule"));
                scheduleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
                infoPanel.add(scheduleLabel);

                // Drop button
                JButton dropButton = new JButton("✗ Drop");
                dropButton.setBackground(new Color(231, 76, 60));
                dropButton.setForeground(Color.WHITE);
                dropButton.setPreferredSize(new Dimension(100, 40));
                dropButton.setFont(new Font("Arial", Font.BOLD, 12));

                int sectionId = Integer.parseInt(section.get("section_id"));
                dropButton.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Drop course:\n\n" +
                                    "Course: " + section.get("course_code") + " - " + section.get("title") + "\n" +
                                    "Instructor: " + section.get("instructor"),
                            "Confirm Drop", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            studentService.dropSection(studentId, sectionId);
                            JOptionPane.showMessageDialog(this, "✓ Course dropped successfully!");
                            handleNavigation("➕ Add/Drop Courses");
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                courseCard.add(infoPanel, BorderLayout.CENTER);
                courseCard.add(dropButton, BorderLayout.EAST);

                coursesPanel.add(courseCard);
                coursesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            JScrollPane scrollPane = new JScrollPane(coursesPanel);
            scrollPane.getVerticalScrollBar().setUnitIncrement(15);
            panel.add(scrollPane, BorderLayout.CENTER);

        } catch (Exception e) {
            panel.add(new JLabel("Error loading courses: " + e.getMessage()), BorderLayout.CENTER);
            e.printStackTrace();
        }

        return panel;
    }

    // GRADES PANEL
//    private void showGradesPanel() {
//        try {
//            JLabel label = new JLabel("📈 My Grades", SwingConstants.CENTER);
//            label.setFont(new Font("Arial", Font.BOLD, 18));
//            mainPanel.add(label, BorderLayout.NORTH);
//
//            List<Map<String, String>> grades = studentService.getGrades(studentId);
//
//            if (grades.isEmpty()) {
//                mainPanel.add(new JLabel("No grades available yet", SwingConstants.CENTER), BorderLayout.CENTER);
//                return;
//            }
//
////            String[] columns = {"Course", "Assessment", "Score", "Max", "Weight %"};
////            Object[][] data = new Object[grades.size()][5];
////
////            for (int i = 0; i < grades.size(); i++) {
////                Map<String, String> grade = grades.get(i);
////                data[i][0] = grade.get("course_code");
////                data[i][1] = grade.get("assessment_type");
////                data[i][2] = grade.get("obtained_score");
////                data[i][3] = grade.get("max_score");
////                data[i][4] = grade.get("weight");
////            }
////
////            JTable table = new JTable(data, columns);
////            table.setRowHeight(25);
////            JScrollPane scrollPane = new JScrollPane(table);
////            mainPanel.add(scrollPane, BorderLayout.CENTER);
//            String[] columns = {"Course", "Assessment", "Score", "Max", "Weight %", "Final CGPA"};
//            Object[][] data = new Object[grades.size()][6];
//
//            for (int i = 0; i < grades.size(); i++) {
//                Map<String, String> grade = grades.get(i);
//                data[i][0] = grade.get("course_code");
//                data[i][1] = grade.get("assessment_type");
//                data[i][2] = grade.get("obtained_score");
//                data[i][3] = grade.get("max_score");
//                data[i][4] = grade.get("weight");
//
//                // Display final grade or status
//                if ("FINALIZED".equals(grade.get("grade_status"))) {
//                    data[i][5] = grade.get("final_grade");
//                } else {
//                    data[i][5] = "In Progress";
//                }
//            }
//        } catch (Exception e) {
//            mainPanel.add(new JLabel("Error loading grades: " + e.getMessage()));
//        }
//    }
//

private void showGradesPanel() {
    mainPanel.removeAll();
    mainPanel.setLayout(new BorderLayout(10, 10));

    JLabel label = new JLabel("📈 My Final Grades & CGPA", SwingConstants.CENTER);
    label.setFont(new Font("Arial", Font.BOLD, 18));
    mainPanel.add(label, BorderLayout.NORTH);

    try {
        List<Map<String, String>> grades = studentService.getFinalizedGrades(studentId);

        if (grades.isEmpty()) {
            JLabel noGradesLabel = new JLabel("No finalized grades available yet.", SwingConstants.CENTER);
            noGradesLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            mainPanel.add(noGradesLabel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
            return;
        }

        // Table setup
        String[] columns = {"Course Code", "Course Title", "Semester", "Year", "Final CGPA"};
        Object[][] data = new Object[grades.size()][5];

        for (int i = 0; i < grades.size(); i++) {
            Map<String, String> grade = grades.get(i);
            data[i][0] = grade.get("course_code");
            data[i][1] = grade.get("title");
            data[i][2] = grade.get("semester");
            data[i][3] = grade.get("year");
            data[i][4] = grade.get("cgpa");
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Download button
        JButton downloadButton = new JButton("📥 Download Grades as CSV");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 14));
        downloadButton.setBackground(new Color(46, 204, 113));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);

        downloadButton.addActionListener(e -> downloadGradesAsCSV(grades));

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(downloadButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

    } catch (Exception e) {
        mainPanel.add(new JLabel("Error loading grades: " + e.getMessage()), BorderLayout.CENTER);
        e.printStackTrace();
    }

    mainPanel.revalidate();
    mainPanel.repaint();
}

    private void downloadGradesAsCSV(List<Map<String, String>> grades) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Grades CSV");
        fileChooser.setSelectedFile(new File(username + "_final_grades.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(fileToSave)) {
                // Write header
                writer.println("Course Code,Course Title,Semester,Year,Final CGPA");

                // Write data
                for (Map<String, String> grade : grades) {
                    writer.printf("\"%s\",\"%s\",\"%s\",%s,%.2f\n",
                            grade.get("course_code"),
                            grade.get("title"),
                            grade.get("semester"),
                            grade.get("year"),
                            Double.parseDouble(grade.get("cgpa")));
                }

                JOptionPane.showMessageDialog(this,
                        "Grades successfully downloaded to:\n" + fileToSave.getAbsolutePath(),
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


    // PROFILE PANEL
    private void showProfilePanel() {
        JPanel profile = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel titleLabel = new JLabel("👤 Student Profile");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        profile.add(titleLabel, gbc);

        String[][] fields = {
                {"Roll Number:", studentProfile.get("roll_number")},
                {"Name:", studentProfile.get("first_name") + " " + studentProfile.get("last_name")},
                {"Email:", studentProfile.get("email")},
                {"Program:", studentProfile.get("program")},
                {"Year of Study:", studentProfile.get("year_of_study")},
                {"Status:", studentProfile.get("status")}
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
