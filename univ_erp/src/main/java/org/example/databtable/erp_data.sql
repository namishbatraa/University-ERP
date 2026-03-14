DROP DATABASE IF EXISTS university_erp;
CREATE DATABASE university_erp;
USE university_erp;

-- Students table
CREATE TABLE students_bach (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    roll_number VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    program VARCHAR(100) NOT NULL,
    year_of_study INT NOT NULL,
    enrollment_date DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED') DEFAULT 'ACTIVE'
);

-- Instructors table
CREATE TABLE instructors_bach (
    instructor_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    employee_id VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'ON_LEAVE') DEFAULT 'ACTIVE'
);

-- Courses table
CREATE TABLE courses_bach (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(10) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    credits INT NOT NULL,
    department VARCHAR(100) NOT NULL,
    prerequisites TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- Sections table
CREATE TABLE sections_bach (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    instructor_id INT,
    section_number VARCHAR(10) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    schedule_day VARCHAR(20) NOT NULL,
    schedule_time TIME NOT NULL,
    capacity INT NOT NULL,
    enrolled_count INT DEFAULT 0,
    registration_deadline DATE NOT NULL,
    status ENUM('OPEN', 'CLOSED', 'CANCELLED') DEFAULT 'OPEN',
    FOREIGN KEY (course_id) REFERENCES courses_bach(course_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors_bach(instructor_id) ON DELETE SET NULL
);

-- Enrollments table
CREATE TABLE enrollments_bach (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    enrollment_date DATE NOT NULL,
    status ENUM('ENROLLED', 'DROPPED', 'COMPLETED', 'FAILED') DEFAULT 'ENROLLED',
    final_grade VARCHAR(2),
    FOREIGN KEY (student_id) REFERENCES students_bach(student_id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES sections_bach(section_id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (student_id, section_id)
);

-- Grades table
CREATE TABLE grades_bach (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    assessment_type ENUM('QUIZ', 'ASSIGNMENT', 'MIDTERM', 'FINAL', 'PROJECT') NOT NULL,
    max_score DECIMAL(5,2) NOT NULL,
    obtained_score DECIMAL(5,2),
    weight_percentage DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments_bach(enrollment_id) ON DELETE CASCADE
);

-- System settings table
CREATE TABLE settings_bach (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL
);

-- Settings
INSERT INTO settings_bach (setting_key, setting_value) VALUES
('maintenance_mode', 'false'),
('current_semester', 'Fall'),
('current_year', '2025');

-- Students
INSERT INTO students_bach (user_id, roll_number, first_name, last_name, email, program, year_of_study, enrollment_date) VALUES
(3, 'S001', 'stu1', '1', 'john.doe@university.com', 'Computer Science', 2, '2024-08-15'),
(4, 'S002', 'stu2', '1', 'jane.smith@university.com', 'Mathematics', 1, '2025-08-15');

-- Instructors
INSERT INTO instructors_bach (user_id, employee_id, first_name, last_name, email, department, hire_date) VALUES
(2, 'I001', 'Dr. Robert', 'Johnson', 'r.johnson@university.com', 'Computer Science', '2020-08-15'),
(5, 'I002', 'Prof. Emily', 'Brown', 'e.brown@university.com', 'Mathematics', '2018-09-01');

-- Courses
INSERT INTO courses_bach (course_code, title, description, credits, department) VALUES
('CS101', 'Introduction to Programming', 'Basic programming concepts', 3, 'Computer Science'),
('CS201', 'Data Structures', 'Advanced data structures', 4, 'Computer Science'),
('MA201', 'Advanced Calculus', 'Multivariable calculus', 4, 'Mathematics'),
('CS301', 'Database Systems', 'Relational databases', 3, 'Computer Science'),
('MA101', 'Linear Algebra', 'Vectors and matrices', 3, 'Mathematics');

-- Sections
INSERT INTO sections_bach (course_id, instructor_id, section_number, semester, year, schedule_day, schedule_time, capacity, enrolled_count, registration_deadline) VALUES
(1, 1, 'A', 'Fall', 2025, 'Mon/Wed', '10:00:00', 50, 2, '2025-09-01'),
(2, 1, 'B', 'Fall', 2025, 'Tue/Thu', '14:00:00', 40, 1, '2025-09-01'),
(3, 2, 'A', 'Fall', 2025, 'Mon/Wed', '13:00:00', 35, 1, '2025-09-01'),
(4, 1, 'A', 'Fall', 2025, 'Wed/Fri', '11:00:00', 45, 1, '2025-09-01'),
(5, 2, 'A', 'Fall', 2025, 'Tue/Thu', '09:00:00', 40, 0, '2025-09-01');

-- Enrollments
INSERT INTO enrollments_bach (student_id, section_id, enrollment_date, status) VALUES
(1, 1, '2025-08-20', 'ENROLLED'),
(1, 2, '2025-08-20', 'ENROLLED'),
(1, 3, '2025-08-20', 'ENROLLED'),
(1, 4, '2025-08-20', 'ENROLLED');

-- Grades
INSERT INTO grades_bach (enrollment_id, assessment_type, max_score, obtained_score, weight_percentage) VALUES
(1, 'MIDTERM', 100, 88, 30),
(1, 'ASSIGNMENT', 100, 85, 20),
(2, 'MIDTERM', 100, 82, 30),
(2, 'ASSIGNMENT', 100, 90, 20),
(3, 'MIDTERM', 100, 75, 30),
(3, 'ASSIGNMENT', 100, 88, 20);

-- Verify data
SELECT * FROM students_bach;
SELECT * FROM courses_bach;
SELECT * FROM enrollments_bach;

USE university_erp;

USE university_erp;

ALTER TABLE enrollments_bach 
MODIFY COLUMN enrollment_date DATE DEFAULT (CURRENT_DATE);


-- Verify
DESCRIBE enrollments_bach;


