package org.example.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import org.example.auth.AuthService;



public class loginframe extends JFrame {
    // UI
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JButton forgotPasswordButton;
    private JCheckBox showPasswordCheck;
    private JLabel statusLabel;
    private JLabel marqueeLabel;
    private String marqueeText = "Welcome to ERP Page of IIIT Delhi     ";
    private int marqueeIndex = 0;

    // Auth dependency
    private final AuthService authService;

    // Lockout state
    private int failedAttempts = 0;
    private long lockoutUntilMillis = 0L;
    private Timer lockoutTimer;

    // Config
    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_MILLIS = 2 * 60_000L; // 2 minutes
    private static final int TICK_MS = 1000;

    public loginframe() {
        super("University ERP - Login");
        this.authService = new AuthService();
        initUI();
        initTimers();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(450, 320);
        setLocationRelativeTo(null);
    }
//public loginframe() {
//    super("University ERP - Login");
//    this.authService = new AuthService();
//
//    // Load the background image
//    ImageIcon icon = new ImageIcon("/absolute/path/to/background.jpg"); // <- Put your image path here!
//    BackgroundPanel bgPanel = new BackgroundPanel(icon.getImage());
//
//    // Set the content pane to your background panel BEFORE initUI
//    bgPanel.setLayout(new BorderLayout(8,8));
//    setContentPane(bgPanel);
//
//    initUI();         // Build form fields/action panels (add all to bgPanel)
//    initTimers();
//
//    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//    setSize(450, 320);
//    setLocationRelativeTo(null);
//}
//
//    private void startMarquee() {
//        marqueeLabel = new JLabel(marqueeText); // The label that shows the text
//        Font font = new Font("Arial", Font.BOLD, 18); // Big, bold font
//        marqueeLabel.setFont(font);
//        marqueeLabel.setForeground(new Color(41, 128, 185)); // Nice blue color
//
//        // Put the label inside a panel (for easy positioning)
//        JPanel marqueePanel = new JPanel(new BorderLayout());
//        marqueePanel.add(marqueeLabel, BorderLayout.CENTER);
//        ((JPanel)getContentPane()).add(marqueePanel, BorderLayout.NORTH);
//
//        // Tell Java to update the label every 90 ms ("slide" the text)
//        Timer t = new Timer(90, e -> {
//            marqueeIndex = (marqueeIndex + 1) % marqueeText.length(); // Move the start index
//            // Cut the string at 'marqueeIndex' and stitch the ends together so it "slides"
//            String text = marqueeText.substring(marqueeIndex) + marqueeText.substring(0, marqueeIndex);
//            marqueeLabel.setText(text); // Update label with new sliding text
//        });
//
//        t.start(); // Start the automatic sliding!
//    }

    private void startMarquee() {
        marqueeLabel = new JLabel(marqueeText);
        Font font = new Font("Arial", Font.BOLD, 18);
        marqueeLabel.setFont(font);
        marqueeLabel.setForeground(new Color(41, 128, 185));

        JPanel marqueePanel = new JPanel(new BorderLayout());
        marqueePanel.setOpaque(false);
        marqueePanel.add(marqueeLabel, BorderLayout.CENTER);

        // Create a combined top panel with marquee AND status
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(marqueePanel, BorderLayout.NORTH);
        topPanel.add(statusLabel, BorderLayout.SOUTH);  // Add status below marquee

        ((JPanel)getContentPane()).add(topPanel, BorderLayout.NORTH);

        Timer t = new Timer(90, e -> {
            marqueeIndex = (marqueeIndex + 1) % marqueeText.length();
            String text = marqueeText.substring(marqueeIndex) + marqueeText.substring(0, marqueeIndex);
            marqueeLabel.setText(text);
        });
        t.start();
    }


    private void initUI() {
        // Main panel with padding

        ImageIcon icon = new ImageIcon("/Users/shashank_mona/Downloads/univ_erp/src/main/java/org/example/logo.png"); // <-- Use your image filename or path
        BackgroundPanel bgPanel = new BackgroundPanel(icon.getImage());
        bgPanel.setLayout(new BorderLayout(8,8));
        setContentPane(bgPanel);
//        JPanel bgPanel = new JPanel(new BorderLayout(8,8));
//        bgPanel.setBackground(new Color(240, 240, 240)); // Light gray background, or use any color you want
//        setContentPane(bgPanel);


        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        linksPanel.add(makeLinkLabel("About IIITD", "https://www.iiitd.ac.in/"));
        linksPanel.add(makeLinkLabel("Past Placements", "https://iiitd.ac.in/placement"));
        linksPanel.add(makeLinkLabel("Teaching Staffs", "https://iiitd.ac.in/people/faculty"));
        linksPanel.setOpaque(false);




        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185));
        JLabel headerLabel = new JLabel("🎓 University ERP System");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;

        // Username
        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Username:"), gc);
        usernameField = new JTextField(20);
        gc.gridx = 1; gc.gridy = 0;
        form.add(usernameField, gc);

        // Password
        gc.gridx = 0; gc.gridy = 1;
        form.add(new JLabel("Password:"), gc);
        passwordField = new JPasswordField(20);
        gc.gridx = 1; gc.gridy = 1;
        form.add(passwordField, gc);

        // Show password
        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.addActionListener(e -> togglePasswordEcho());
        gc.gridx = 1; gc.gridy = 2;
        form.add(showPasswordCheck, gc);

        // Forgot password link
        forgotPasswordButton = new JButton("Forgot Password?");
        forgotPasswordButton.setForeground(new Color(41, 128, 185));
        forgotPasswordButton.setBorderPainted(false);
        forgotPasswordButton.setContentAreaFilled(false);
        forgotPasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordButton.addActionListener(e -> showForgotPasswordDialog());
        gc.gridx = 1; gc.gridy = 3;
        form.add(forgotPasswordButton, gc);

        // Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setOpaque(false);
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(this::onLogin);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onCancel());

        actions.add(loginButton);
        actions.add(cancelButton);

        // Status
//        statusLabel = new JLabel(" ");
//        statusLabel.setForeground(new Color(180, 0, 0));
//        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED); // Use pure red for better visibility
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Make it bold and larger
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(true); // Make background opaque
        statusLabel.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white background


        // Layout
        mainPanel.add(form, BorderLayout.CENTER);
        mainPanel.add(actions, BorderLayout.SOUTH);
        mainPanel.add(statusLabel, BorderLayout.NORTH);

       // Or where you want

//        setLayout(new BorderLayout());
//        add(headerPanel, BorderLayout.NORTH);
//        add(mainPanel, BorderLayout.CENTER);

        bgPanel.setLayout(new BorderLayout(8,8));
        bgPanel.add(form, BorderLayout.CENTER);
        bgPanel.add(actions, BorderLayout.SOUTH);
        bgPanel.add(statusLabel, BorderLayout.NORTH);
        bgPanel.add(linksPanel, BorderLayout.SOUTH);
        setContentPane(bgPanel);


        // Default button
        getRootPane().setDefaultButton(loginButton);

        startMarquee();
    }

//    public class BackgroundPanel extends JPanel {
//        private final Image bg;
//        public BackgroundPanel(Image img) {
//            this.bg = img;
//        }
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            // Scales image to fill window, prevents partial showing
//            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
//        }
//    }
public class BackgroundPanel extends JPanel {
    private final Image bg;
    private float opacity = 0.5f; // set between 0.0f (fully transparent) and 1.0f (fully opaque)

    public BackgroundPanel(Image img) {
        this.bg = img;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity)); // clamp between 0 and 1
        repaint(); // update panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bg != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}



    private void initTimers() {
        lockoutTimer = new Timer(TICK_MS, e -> updateLockoutCountdown());
        lockoutTimer.setRepeats(true);
    }

    private void togglePasswordEcho() {
        if (showPasswordCheck.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('•');
        }
    }

    private JLabel makeLinkLabel(String text, String url) {
        JLabel link = new JLabel("<html><a href=''>" + text + "</a></html>");
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        link.setForeground(Color.BLUE);
        link.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                } catch (Exception ex) { /* handle exception */ }
            }
        });
        return link;
    }




    private void onLogin(ActionEvent evt) {
        long now = System.currentTimeMillis();
        if (now < lockoutUntilMillis) {
            ensureTimerRunning();
            JOptionPane.showMessageDialog(this, "Too many attempts. Please wait for the cooldown to end.");
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Please enter username and password.");
            return;
        }

        try {
            String userInfo = authService.authenticate(username, password);

            if (userInfo != null) {
                String role = userInfo;
                failedAttempts = 0;
                setStatus("Login successful.");

                dispose();
                SwingUtilities.invokeLater(() -> {
                    switch(role.toUpperCase()) {
                        case "ADMIN":
                            new admindashboard(username).setVisible(true);
                            break;
                        case "INSTRUCTOR":
                            new InstructorDashboard(username).setVisible(true);
                            break;
                        case "STUDENT":
                            new studentdashboard(username).setVisible(true);
                            break;
                        default:
                            JOptionPane.showMessageDialog(null, "Unknown role: " + role);
                            new loginframe().setVisible(true);
                    }
                });
            } else {
                failedAttempts++;
                if (failedAttempts >= MAX_ATTEMPTS) {
                    startLockout();
                } else {
                    setStatus("Invalid credentials. Attempt " + failedAttempts + " of " + MAX_ATTEMPTS + ".");
                }
            }
        } catch (Exception ex) {
            setStatus("Auth error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void onCancel() {
        usernameField.setText("");
        passwordField.setText("");
        setStatus(" ");
    }

    private void setInputsEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        showPasswordCheck.setEnabled(enabled);
        forgotPasswordButton.setEnabled(enabled);
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    private void startLockout() {
        lockoutUntilMillis = System.currentTimeMillis() + LOCKOUT_MILLIS;
        setInputsEnabled(false);
        ensureTimerRunning();
        updateLockoutCountdown();
    }

    private void ensureTimerRunning() {
        if (!lockoutTimer.isRunning()) lockoutTimer.start();
    }

    private void endLockout() {
        if (lockoutTimer.isRunning()) lockoutTimer.stop();
        lockoutUntilMillis = 0L;
        failedAttempts = 0;
        setInputsEnabled(true);
        setStatus("You can try logging in again.");
    }

    private void updateLockoutCountdown() {
        long now = System.currentTimeMillis();
        if (now >= lockoutUntilMillis) {
            endLockout();
            return;
        }
        long remaining = (lockoutUntilMillis - now) / 1000;
        long mm = remaining / 60;
        long ss = remaining % 60;
        setStatus(String.format("Locked due to failed attempts. Try again in %02d:%02d.", mm, ss));
    }

    // FORGOT PASSWORD DIALOG
//    private void showForgotPasswordDialog() {
//        JDialog dialog = new JDialog(this, "Forgot Password", true);
//        dialog.setLayout(new BorderLayout(10, 10));
//        dialog.setSize(400, 250);
//        dialog.setLocationRelativeTo(this);
//
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(10, 10, 10, 10);
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//
//        JLabel infoLabel = new JLabel("<html>Enter your username to receive OTP<br>on your registered phone number</html>");
//        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
//        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
//        panel.add(infoLabel, gbc);
//
//        JLabel usernameLabel = new JLabel("Username:");
//        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
//        panel.add(usernameLabel, gbc);
//
//        JTextField usernameInput = new JTextField(20);
//        gbc.gridx = 1; gbc.gridy = 1;
//        panel.add(usernameInput, gbc);
//
//        JButton sendOTPButton = new JButton("Send OTP");
//        sendOTPButton.setBackground(new Color(52, 152, 219));
//        sendOTPButton.setForeground(Color.WHITE);
//        sendOTPButton.addActionListener(e -> {
//            String username = usernameInput.getText().trim();
//            if (username.isEmpty()) {
//                JOptionPane.showMessageDialog(dialog, "Please enter username");
//                return;
//            }
//
//            try {
//                if (!authService.userExists(username)) {
//                    JOptionPane.showMessageDialog(dialog, "Username not found");
//                    return;
//                }
//
//                String phone = authService.getPhoneNumber(username);
//                if (phone == null || phone.isEmpty()) {
//                    JOptionPane.showMessageDialog(dialog, "No phone number registered for this account");
//                    return;
//                }
//
//                String otp = authService.generateOTP(username);
//
//                // In production, OTP would be sent via SMS
//                // For demo, we'll show it in a dialog
//                JOptionPane.showMessageDialog(dialog,
//                        "OTP sent to phone: " + maskPhone(phone) + "\n\n" +
//                                "Demo OTP Code: " + otp + "\n\n" +
//                                "(In production, this would be sent via SMS)",
//                        "OTP Sent", JOptionPane.INFORMATION_MESSAGE);
//
//                dialog.dispose();
//                showVerifyOTPDialog(username);
//
//            } catch (Exception ex) {
//                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//
//        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
//        panel.add(sendOTPButton, gbc);
//
//        dialog.add(panel, BorderLayout.CENTER);
//        dialog.setVisible(true);
//    }



    private void showForgotPasswordDialog() {
        JDialog dialog = new JDialog(this, "Forgot Password - Firebase OTP", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 280);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel infoLabel = new JLabel("<html>Enter your username to receive OTP<br>via SMS using Firebase</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(infoLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(usernameLabel, gbc);

        JTextField usernameInput = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(usernameInput, gbc);

        JButton sendOTPButton = new JButton("Send OTP via Firebase");
        sendOTPButton.setBackground(new Color(52, 152, 219));
        sendOTPButton.setForeground(Color.WHITE);
        sendOTPButton.addActionListener(e -> {
            String username = usernameInput.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter username");
                return;
            }

            try {
                if (!authService.userExists(username)) {
                    JOptionPane.showMessageDialog(dialog, "Username not found");
                    return;
                }

                String phone = authService.getPhoneNumber(username);
                if (phone == null || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "No phone number registered");
                    return;
                }

                String otp = authService.sendOTPViaSMS(username, phone);

                JOptionPane.showMessageDialog(dialog,
                        "✅ OTP sent via Firebase SMS to: " + maskPhone(phone) +
                                "\n\nDEMO OTP: " + otp +
                                "\n(Remove this in production)",
                        "OTP Sent", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                showVerifyOTPDialog(username);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(sendOTPButton, gbc);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }






    private void showVerifyOTPDialog(String username) {
        JDialog dialog = new JDialog(this, "Verify OTP", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel infoLabel = new JLabel("Enter the OTP sent to your phone");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(infoLabel, gbc);

        JLabel otpLabel = new JLabel("OTP Code:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(otpLabel, gbc);

        JTextField otpInput = new JTextField(10);
        otpInput.setFont(new Font("Monospaced", Font.BOLD, 16));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(otpInput, gbc);

        JLabel newPassLabel = new JLabel("New Password:");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(newPassLabel, gbc);

        JPasswordField newPassField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(newPassField, gbc);

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(confirmPassLabel, gbc);

        JPasswordField confirmPassField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(confirmPassField, gbc);

        JButton resetButton = new JButton("Reset Password");
        resetButton.setBackground(new Color(46, 204, 113));
        resetButton.setForeground(Color.WHITE);
        resetButton.addActionListener(e -> {
            String otp = otpInput.getText().trim();
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());

            if (otp.isEmpty() || newPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, "Passwords don't match");
                return;
            }

            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 6 characters");
                return;
            }

            try {
                if (authService.verifyOTP(username, otp)) {
                    authService.changePassword(username, newPass);
                    JOptionPane.showMessageDialog(dialog,
                            "Password changed successfully!\nYou can now login with your new password.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Invalid or expired OTP. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(resetButton, gbc);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        int len = phone.length();
        return "******" + phone.substring(len - 4);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new loginframe().setVisible(true));
    }
}
