package org.example;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptDemo {
    public static void main(String[] args) {
        String passwordPlaintext = "jalote";
        // 12 rounds of hashing
        String hashed = BCrypt.hashpw(passwordPlaintext, BCrypt.gensalt(12));
        System.out.println("Hashed password: " + hashed);
    }
}

