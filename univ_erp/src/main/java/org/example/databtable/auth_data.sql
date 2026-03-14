DROP DATABASE IF EXISTS university_auth;
CREATE DATABASE university_auth;
USE university_auth;

-- Users authentication table
CREATE TABLE users_auth (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    role ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- BCrypt hash (includes salt internally)
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') DEFAULT 'ACTIVE',
    failed_login_attempts INT DEFAULT 0,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Login attempts log for security monitoring
CREATE TABLE login_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL,
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_agent TEXT
);

-- Indexes
CREATE INDEX idx_username ON users_auth(username);
CREATE INDEX idx_role ON users_auth(role);
CREATE INDEX idx_status ON users_auth(status);

-- Sample data (password for all: "password123")
INSERT INTO users_auth (username, role, password_hash) VALUES
('admin', 'ADMIN', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('instructor1', 'INSTRUCTOR', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('student1', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('student2', 'STUDENT', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

SELECT * FROM users_auth;

UPDATE users_auth
SET password_hash = '$2a$12$hCHoqtOJUfnuY7EJFTymzeW4BERP48KWox23DJpX.lA6EgWwhvE02'  -- hash for “password”
WHERE username = 'student1';

UPDATE users_auth
SET password_hash = '$2a$12$mLyvtP8DlxtMJpikkCSTAuNxotu1a0HA86cbBhc8Qht7pI5LiB/MW'  -- hash for “password”
WHERE username = 'admin';



USE university_erp;

-- Create notifications table
CREATE TABLE IF NOT EXISTS notifications_bach (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    notification_type ENUM('GRADE_POSTED', 'COURSE_ADDED', 'ANNOUNCEMENT', 'GENERAL') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students_bach(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




USE university_auth;

-- Create OTP verification table
CREATE TABLE IF NOT EXISTS otp_verification (
    otp_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    INDEX idx_username_otp (username, otp_code),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE university_auth;

DESCRIBE users_auth;

USE university_auth;

-- 2. Create OTP verification table
CREATE TABLE IF NOT EXISTS otp_verification (
    otp_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    INDEX idx_username_otp (username, otp_code),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


DESCRIBE users_auth;

DESCRIBE users_auth;

USE university_auth;

ALTER TABLE users_auth 

ADD COLUMN phone VARCHAR(15) NULL AFTER user_id;

CREATE TABLE IF NOT EXISTS otp_verification (
    otp_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    INDEX idx_username_otp (username, otp_code),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DESCRIBE users_auth;
UPDATE users_auth SET phone = '9311309475' WHERE username = 'admin';


