package com.studyflow.service;

import com.studyflow.dao.CourseDao;
import com.studyflow.model.Course;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Business logic for courses.
 *
 * <p>Thin on purpose: a course has few rules beyond the validation the
 * {@link Course} model already enforces in its constructor. The value of this
 * layer is that the UI depends on it — not on a DAO — so persistence can change
 * without touching a single screen, and every method here can be unit-tested
 * with a mocked {@link CourseDao}.
 */
public class CourseService {

    private final CourseDao courseDao;

    public CourseService(CourseDao courseDao) {
        this.courseDao = Objects.requireNonNull(courseDao, "courseDao must not be null");
    }

    /**
     * Creates a new course for a user.
     *
     * <p>Field validation (blank name, non-positive user id) is delegated to the
     * {@link Course} constructor, so an invalid course can never reach the DAO.
     */
    public Course createCourse(int userId, String name, String code, String semester) {
        Course course = new Course(userId, name, code, semester);
        return courseDao.create(course);
    }

    public Optional<Course> findById(int id) {
        return courseDao.findById(id);
    }

    public List<Course> findAll() {
        return courseDao.findAll();
    }

    public List<Course> findByUser(int userId) {
        return courseDao.findByUserId(userId);
    }

    public boolean updateCourse(Course course) {
        Objects.requireNonNull(course, "course must not be null");
        return courseDao.update(course);
    }

    /**
     * Deletes a course. Its tasks (and their subtasks and reminders) are removed
     * by the cascading foreign keys defined in the schema.
     *
     * @return {@code true} if a course was deleted
     */
    public boolean deleteCourse(int id) {
        return courseDao.deleteById(id);
    }
}
