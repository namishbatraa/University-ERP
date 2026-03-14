package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class LoginFrame extends JFrame {
    // UI
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JCheckBox showPasswordCheck;
    private JLabel statusLabel;

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

    public LoginFrame() {
        super("Login");
        AuthService authService = new AuthService();
        this.authService = Objects.requireNonNull(authService, "authService required");
        initUI();
        initTimers();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        // Panels
        JPanel form = new JPanel(new GridBagLayout());
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

        // Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        loginButton = new JButton("Login");
        loginButton.addActionListener(this::onLogin);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onCancel());
        actions.add(loginButton);
        actions.add(cancelButton);

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(180, 0, 0));

        // Layout frame
        setLayout(new BorderLayout(8, 8));
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);

        // Default button
        getRootPane().setDefaultButton(loginButton);
    }

    private void initTimers() {
        lockoutTimer = new Timer(TICK_MS, e -> updateLockoutCountdown());
        lockoutTimer.setRepeats(true);
    }

    private void togglePasswordEcho() {
        if (showPasswordCheck.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar((Character) UIManager.get("PasswordField.echoChar"));        }
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

        String ok;
        try {
            ok = authService.authenticate(username, password);
        } catch (Exception ex) {
            setStatus("Auth error: " + ex.getMessage());
            return;
        }

        if (ok!=null) {
            failedAttempts = 0;
            setStatus("Login successful.");
            // TODO: proceed to next screen
            JOptionPane.showMessageDialog(this, "Welcome, " + username + "!");
            dispose();
        } else {
            failedAttempts++;
            if (failedAttempts >= MAX_ATTEMPTS) {
                startLockout();
            } else {
                setStatus("Invalid credentials. Attempt " + failedAttempts + " of " + MAX_ATTEMPTS + ".");
            }
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
        cancelButton.setEnabled(enabled);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
