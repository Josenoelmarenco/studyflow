package com.studyflow.service;

import com.studyflow.dao.TaskDao;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Business logic for tasks.
 *
 * <p>This layer exists so that rules like "what counts as overdue" or "what
 * belongs on the dashboard" live in one place instead of being scattered across
 * UI controllers. The UI asks this class questions; it never talks to a DAO.
 *
 * <p>It depends on the {@link TaskDao} <em>interface</em>, so its tests can
 * supply a mock and run in milliseconds with no database at all.
 */
public class TaskService {

    private final TaskDao taskDao;

    public TaskService(TaskDao taskDao) {
        this.taskDao = Objects.requireNonNull(taskDao, "taskDao must not be null");
    }

    /**
     * Creates a new task in {@link TaskStatus#PENDING}.
     *
     * @throws IllegalArgumentException if the deadline is in the past
     */
    public Task createTask(int courseId, String title, String description,
                           LocalDateTime deadline, LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");

        if (deadline != null && deadline.isBefore(now)) {
            throw new IllegalArgumentException("Deadline cannot be in the past: " + deadline);
        }

        Task task = new Task(courseId, title, description, deadline, TaskStatus.PENDING);
        return taskDao.create(task);
    }

    /**
     * Persists a fully-specified new task exactly as given.
     *
     * <p>Unlike {@link #createTask}, this makes no assumptions: the caller
     * chooses the status and may record a task whose deadline is already in the
     * past (for example, logging an assignment that is late). Field validation is
     * still enforced by the {@link Task} constructor. Used by the edit/create
     * form in the UI, where the user picks every field.
     */
    public Task saveNew(Task task) {
        Objects.requireNonNull(task, "task must not be null");
        return taskDao.create(task);
    }

    public Optional<Task> findById(int id) {
        return taskDao.findById(id);
    }

    public List<Task> findAll() {
        return taskDao.findAll();
    }

    public List<Task> findByCourse(int courseId) {
        return taskDao.findByCourseId(courseId);
    }

    /**
     * Tasks whose deadline has passed and that are not finished.
     *
     * @param now the reference time, injected so the rule is testable
     */
    public List<Task> findOverdue(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");

        return taskDao.findAll().stream()
                .filter(task -> task.isOverdue(now))
                .sorted(Comparator.comparing(Task::getDeadline))
                .toList();
    }

    /**
     * Unfinished tasks due within the next {@code days} days.
     *
     * @param days how far ahead to look; must be positive
     */
    public List<Task> findUpcoming(LocalDateTime now, int days) {
        Objects.requireNonNull(now, "now must not be null");
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive, was: " + days);
        }

        LocalDateTime cutoff = now.plusDays(days);

        return taskDao.findAll().stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .filter(task -> task.getDeadline() != null)
                .filter(task -> !task.getDeadline().isBefore(now))
                .filter(task -> !task.getDeadline().isAfter(cutoff))
                .sorted(Comparator.comparing(Task::getDeadline))
                .toList();
    }

    /**
     * Marks a task as done.
     *
     * @return {@code true} if the task existed and was updated
     */
    public boolean completeTask(int id) {
        return taskDao.findById(id)
                .map(task -> {
                    task.markDone();
                    return taskDao.update(task);
                })
                .orElse(false);
    }

    public boolean updateTask(Task task) {
        return taskDao.update(task);
    }

    public boolean deleteTask(int id) {
        return taskDao.deleteById(id);
    }

    /**
     * Share of tasks that are finished, as a value between 0.0 and 1.0.
     *
     * <p>Returns 0.0 when there are no tasks — a student with an empty planner
     * is at 0% progress, not at an undefined division by zero.
     */
    public double completionRate() {
        List<Task> all = taskDao.findAll();
        if (all.isEmpty()) {
            return 0.0;
        }

        long done = all.stream()
                .filter(task -> task.getStatus() == TaskStatus.DONE)
                .count();

        return (double) done / all.size();
    }

    /** Number of tasks in each state, for the dashboard counters. */
    public long countByStatus(TaskStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return taskDao.findByStatus(status).size();
    }
}
