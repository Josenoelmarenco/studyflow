package com.studyflow.model;

/**
 * A small checklist item belonging to a {@link Task}.
 *
 * <p>Subtasks let a big assignment ("Sprint 2 report") be broken into concrete
 * steps ("collect team hours", "write retrospective"). They are the unit the
 * progress bar on the dashboard is computed from.
 *
 * <p>Like {@link Task}, validation lives in the constructor: a subtask can never
 * exist with a blank title or an impossible parent id.
 */
public class Subtask {

    private int id;
    private int taskId;
    private String title;
    private boolean done;

    /** Creates an unsaved, not-yet-finished subtask. */
    public Subtask(int taskId, String title) {
        this(0, taskId, title, false);
    }

    public Subtask(int id, int taskId, String title, boolean done) {
        this.id = id;
        setTaskId(taskId);
        setTitle(title);
        this.done = done;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public final void setTaskId(int taskId) {
        if (taskId <= 0) {
            throw new IllegalArgumentException("taskId must be positive, was: " + taskId);
        }
        this.taskId = taskId;
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

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    /** Flips between done and not-done — used by the checkbox in the UI. */
    public void toggle() {
        this.done = !this.done;
    }

    @Override
    public String toString() {
        return "Subtask{id=%d, taskId=%d, title='%s', done=%s}"
                .formatted(id, taskId, title, done);
    }
}
