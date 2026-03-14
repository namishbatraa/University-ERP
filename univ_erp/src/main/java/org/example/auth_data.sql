
CREATE DATABASE IF NOT EXISTS university_auth;
USE university_auth;

-- Users authentication table
CREATE TABLE users_auth (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    role ENUM('STUDENT', 'INSTRUCTOR', 'ADMIN') NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
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

-- Create indexes for performance
CREATE INDEX idx_username ON users_auth(username);
CREATE INDEX idx_role ON users_auth(role);
CREATE INDEX idx_status ON users_auth(status);

INSERT INTO users_auth (username, role, password_hash, salt) VALUES
('admin1', 'ADMIN', '$2a$10$E9oi1NJmSBHUb7YQFHvfo.Ek6w2IW0rLXyUy2djcaU3zqREAqj386', 'randomsalt1'),
('inst1', 'INSTRUCTOR', '$2a$10$OeBoklVmO/Z6ZOE/FquWmOT6BzQGhx5ne.tarHbKCKpk7zzU8c7eK', 'randomsalt2'),
('stu1', 'STUDENT', '$2a$10$jKtDsfCBvN0OkPvuqDt0lOtV2JJ9oskJR4vknRAaCD9EnMxyeuNkq', 'randomsalt3'),
('stu2', 'STUDENT', '$2a$10$7.x0sX9.V.bL9QZ3iY.NnO/Y3C0bW.d2V/6f7e4c5b6a7d8e9f', 'randomsalt4');
SELECT * FROM users_auth;
UPDATE users_auth
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE username = 'stu1';
SELECT * FROM users_auth;
UPDATE users_auth
SET password_hash = '$2a$12$8bEDxCzKoZw/B/KcVTf8tO2qJYsRcX1oexVBsXIDgHmmNZQBokgtC'
WHERE username = 'stu1';
SELECT * FROM users_auth;
UPDATE users_auth
SET password_hash = '$2a$12$J.G0.H2cK7b4oa4gwDIAnOLXoKjWNSmG.Hu/0QsVbfF/si6BIPrDC'
WHERE username = 'stu2';
SELECT * FROM users_auth;
SELECT * FROM users_auth;
SELECT * FROM users_auth;
UPDATE users_auth
SET password_hash = '$2a$12$J.G0.H2cK7b4oa4gwDIAnOLXoKjWNSmG.Hu/0QsVbfF/si6BIPrDC'
WHERE username = 'stu2';
SELECT * FROM users_auth;
