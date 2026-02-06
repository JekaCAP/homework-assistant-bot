-- Создание последовательностей
CREATE SEQUENCE IF NOT EXISTS student_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS course_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS assignment_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS submission_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS admin_seq START 1 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS progress_seq START 1 INCREMENT 1;

-- Таблица студентов
CREATE TABLE students (
                          id BIGINT PRIMARY KEY DEFAULT nextval('student_seq'),
                          telegram_id BIGINT UNIQUE NOT NULL,
                          telegram_username VARCHAR(100),
                          full_name VARCHAR(200) NOT NULL,
                          github_username VARCHAR(100),
                          email VARCHAR(150),
                          registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          is_active BOOLEAN DEFAULT TRUE,
                          last_activity TIMESTAMP,
                          CONSTRAINT valid_telegram_id CHECK (telegram_id > 0),
                          CONSTRAINT valid_email CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- Таблица админов
CREATE TABLE admins (
                        id BIGINT PRIMARY KEY DEFAULT nextval('admin_seq'),
                        telegram_id BIGINT UNIQUE NOT NULL,
                        telegram_username VARCHAR(100),
                        full_name VARCHAR(200) NOT NULL,
                        email VARCHAR(150),
                        role VARCHAR(50) NOT NULL DEFAULT 'REVIEWER',
                        permissions JSONB DEFAULT '["VIEW_SUBMISSIONS", "REVIEW_HOMEWORK"]'::jsonb,
                        is_active BOOLEAN DEFAULT TRUE,
                        last_login TIMESTAMP,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица курсов
CREATE TABLE courses (
                         id BIGINT PRIMARY KEY DEFAULT nextval('course_seq'),
                         code VARCHAR(50) UNIQUE NOT NULL,
                         name VARCHAR(200) NOT NULL,
                         description TEXT,
                         icon VARCHAR(50),
                         is_active BOOLEAN DEFAULT TRUE,
                         sort_order INT DEFAULT 0,
                         difficulty_level VARCHAR(20) DEFAULT 'BEGINNER',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица домашних заданий
CREATE TABLE assignments (
                             id BIGINT PRIMARY KEY DEFAULT nextval('assignment_seq'),
                             course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                             number INT NOT NULL,
                             title VARCHAR(300) NOT NULL,
                             description TEXT,
                             requirements JSONB DEFAULT '[]'::jsonb,
                             max_score INT DEFAULT 100,
                             min_score INT DEFAULT 0,
                             deadline TIMESTAMP,
                             is_active BOOLEAN DEFAULT TRUE,
                             github_template_url VARCHAR(500),
                             test_command VARCHAR(200),
                             estimated_hours INT,
                             type VARCHAR(20) DEFAULT 'HOMEWORK',
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT unique_course_assignment UNIQUE(course_id, number)
);

-- Таблица сдачи работ
CREATE TABLE submissions (
                             id BIGINT PRIMARY KEY DEFAULT nextval('submission_seq'),
                             student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
                             assignment_id BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
                             pr_url VARCHAR(500) NOT NULL,
                             pr_number INT,
                             github_repo VARCHAR(300),
                             commit_hash VARCHAR(100),
                             branch_name VARCHAR(100),
                             status VARCHAR(20) DEFAULT 'SUBMITTED',
                             score INT CHECK (score >= 0 AND score <= 100),
                             reviewer_comment TEXT,
                             student_comment TEXT,
                             auto_checks_passed BOOLEAN,
                             submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             reviewed_at TIMESTAMP,
                             resubmitted_at TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             reviewer_id BIGINT REFERENCES admins(id),
                             version BIGINT DEFAULT 0,
                             CONSTRAINT valid_pr_url CHECK (pr_url LIKE 'https://github.com/%/pull/%'),
                             CONSTRAINT unique_student_assignment UNIQUE(student_id, assignment_id)
);

-- Таблица прогресса студентов
CREATE TABLE student_progress (
                                  id BIGINT PRIMARY KEY DEFAULT nextval('progress_seq'),
                                  student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
                                  course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
                                  assignments_submitted INT DEFAULT 0,
                                  assignments_accepted INT DEFAULT 0,
                                  assignments_rejected INT DEFAULT 0,
                                  total_score INT DEFAULT 0,
                                  average_score DECIMAL(5,2) DEFAULT 0.00,
                                  completion_percentage DECIMAL(5,2) DEFAULT 0.00,
                                  rank_position INT,
                                  last_submission_date TIMESTAMP,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT unique_student_course_progress UNIQUE(student_id, course_id)
);

-- Таблица логов действий
CREATE TABLE activity_logs (
                               id BIGINT PRIMARY KEY DEFAULT nextval('log_seq'),
                               user_type VARCHAR(20) NOT NULL,
                               user_id BIGINT NOT NULL,
                               action VARCHAR(100) NOT NULL,
                               description TEXT,
                               details JSONB,
                               ip_address VARCHAR(45),
                               user_agent TEXT,
                               telegram_chat_id BIGINT,
                               success BOOLEAN DEFAULT TRUE,
                               error_message TEXT,
                               execution_time_ms BIGINT,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);