-- StudyFlow — local MariaDB bootstrap
--
-- Run this ONCE on your local machine before starting the application:
--     mysql -u root -p < db/init-local.sql
--     mysql -u root -p studyflow < db/schema.sql
--
-- Replace 'change-me' with your own password and put the same value in
-- src/main/resources/config.properties (which is git-ignored).

CREATE DATABASE IF NOT EXISTS studyflow
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'studyflow'@'localhost' IDENTIFIED BY 'change-me';

GRANT ALL PRIVILEGES ON studyflow.* TO 'studyflow'@'localhost';

FLUSH PRIVILEGES;
