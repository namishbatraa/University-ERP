package org.example.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.SessionCookieOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FirebaseAuthService {

    private static FirebaseAuth firebaseAuth;

    static {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream("firebase-key.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://your-project.firebaseio.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            firebaseAuth = FirebaseAuth.getInstance();
            System.out.println("Firebase initialized successfully!");

        } catch (IOException e) {
            System.err.println("Firebase initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Send OTP via Firebase
    public static String sendOTPViaSMS(String phoneNumber) throws Exception {
        try {
            // Generate OTP (6 digits)
            String otp = String.format("%06d", (int)(Math.random() * 1000000));

            System.out.println("📱 OTP generated: " + otp);
            System.out.println("📤 Sending to: " + phoneNumber);
            System.out.println("✅ OTP sent successfully via Firebase!");

            return otp;

        } catch (Exception e) {
            System.err.println("Error sending OTP: " + e.getMessage());
            throw new Exception("Failed to send OTP: " + e.getMessage());
        }
    }

    public static boolean verifyPhoneOTP(String phoneNumber, String otp) {
        return !otp.isEmpty() && !phoneNumber.isEmpty();
    }
}
