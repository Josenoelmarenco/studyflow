package com.studyflow.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An assignment or exam belonging to a course.
 *
 * <p>Two deliberate choices here:
 *
 * <ul>
 *   <li><b>{@link LocalDateTime} instead of {@code java.sql.Timestamp}.</b>
 *       {@code Timestamp} is a JDBC type; letting it leak into the domain model
 *       couples the whole application to the database layer. Conversion happens
 *       in the DAO, where it belongs.</li>
 *   <li><b>Validation in the constructor.</b> A {@code Task} cannot be
 *       constructed in an invalid state, so no other layer has to re-check that
 *       the title is non-empty.</li>
 * </ul>
 *
 * <p>{@code id} is 0 until the row is persisted, at which point the database
 * assigns the real value.
 */
public class Task {

    private int id;
    private int courseId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private TaskStatus status;

    /** Creates an unsaved task (no id yet). */
    public Task(int courseId, String title, String description,
                LocalDateTime deadline, TaskStatus status) {
        this(0, courseId, title, description, deadline, status);
    }

    /** Creates a task with a known id, typically when loading from the database. */
    public Task(int id, int courseId, String title, String description,
                LocalDateTime deadline, TaskStatus status) {
        this.id = id;
        setCourseId(courseId);
        setTitle(title);
        this.description = description;
        this.deadline = deadline;
        setStatus(status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public final void setCourseId(int courseId) {
        if (courseId <= 0) {
            throw new IllegalArgumentException("courseId must be positive, was: " + courseId);
        }
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public final void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be null or blank");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public final void setStatus(TaskStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    /**
     * Whether the deadline has passed while the task is still unfinished.
     *
     * <p>Takes the current time as a parameter rather than calling
     * {@code LocalDateTime.now()} internally — that is what makes this method
     * testable without freezing the system clock.
     *
     * @param now the reference point, usually {@code LocalDateTime.now()}
     * @return {@code true} if the task is overdue
     */
    public boolean isOverdue(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");
        return status != TaskStatus.DONE
                && deadline != null
                && deadline.isBefore(now);
    }

    /** Marks the task as finished. */
    public void markDone() {
        this.status = TaskStatus.DONE;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Task task)) {
            return false;
        }
        // Unsaved tasks (id 0) are only equal to themselves.
        return id != 0 && id == task.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{id=%d, courseId=%d, title='%s', deadline=%s, status=%s}"
                .formatted(id, courseId, title, deadline, status);
    }
}
