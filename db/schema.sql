-- StudyFlow — database schema
--
-- Portable DDL: this file is executed both against MariaDB (runtime)
-- and against H2 in MariaDB compatibility mode (integration tests).
-- For that reason it contains TABLE definitions only — no CREATE DATABASE,
-- no USE statement, and no credentials.
--
-- To bootstrap a local MariaDB instance, see db/init-local.sql

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS courses (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id  INT NOT NULL,
    name     VARCHAR(100) NOT NULL,
    code     VARCHAR(50),
    semester VARCHAR(50),
    CONSTRAINT fk_courses_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tasks (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    course_id   INT NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    deadline    TIMESTAMP NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_tasks_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS subtasks (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    title   VARCHAR(255) NOT NULL,
    is_done BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_subtasks_task
        FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reminders (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    task_id   INT NOT NULL,
    remind_at TIMESTAMP NOT NULL,
    is_sent   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reminders_task
        FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
