-- StudyFlow — sample data for local development and demos
-- Run after schema.sql:
--     mysql -u studyflow -p studyflow < db/seed.sql

INSERT INTO users (name, email, password_hash) VALUES
    ('Demo Student', 'demo@studyflow.local', 'not-a-real-hash');

INSERT INTO courses (user_id, name, code, semester) VALUES
    (1, 'Ohjelmistotuotantoprojekti 1', 'TX00EY27', 'Autumn 2026'),
    (1, 'Suunnittelumallit',            'TX00EY29', 'Autumn 2026'),
    (1, 'WebAssembly with Rust',        'TX00GN69', 'Autumn 2026');

INSERT INTO tasks (course_id, title, description, deadline, status) VALUES
    (1, 'Sprint 2 report',      'Write the sprint review report',      '2026-09-15 12:00:00', 'IN_PROGRESS'),
    (1, 'Jenkins pipeline',     'Set up build + test + coverage',      '2026-09-28 23:59:00', 'PENDING'),
    (2, 'Observer pattern lab', 'Implement the observer exercise',     '2026-09-20 23:59:00', 'PENDING'),
    (3, 'Rust ownership tasks', 'Complete the Viope exercise set',     '2026-09-10 23:59:00', 'DONE');

INSERT INTO subtasks (task_id, title, is_done) VALUES
    (1, 'Collect team hours',   TRUE),
    (1, 'Write retrospective',  FALSE);

INSERT INTO reminders (task_id, remind_at, is_sent) VALUES
    (1, '2026-09-14 18:00:00', FALSE),
    (2, '2026-09-26 09:00:00', FALSE);
