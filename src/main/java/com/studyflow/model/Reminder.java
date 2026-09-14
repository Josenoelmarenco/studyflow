package com.studyflow.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A scheduled nudge for a {@link Task}: "remind me about this at 18:00".
 *
 * <p>A reminder is <em>due</em> when its moment has arrived and it has not been
 * delivered yet. As with {@link Task#isOverdue(LocalDateTime)}, the current time
 * is passed in rather than read from the clock, so "is this reminder due?" is a
 * deterministic, testable question instead of something that depends on when the
 * test happens to run.
 */
public class Reminder {

    private int id;
    private int taskId;
    private LocalDateTime remindAt;
    private boolean sent;

    /** Creates an unsaved, not-yet-sent reminder. */
    public Reminder(int taskId, LocalDateTime remindAt) {
        this(0, taskId, remindAt, false);
    }

    public Reminder(int id, int taskId, LocalDateTime remindAt, boolean sent) {
        this.id = id;
        setTaskId(taskId);
        setRemindAt(remindAt);
        this.sent = sent;
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

    public LocalDateTime getRemindAt() {
        return remindAt;
    }

    public final void setRemindAt(LocalDateTime remindAt) {
        this.remindAt = Objects.requireNonNull(remindAt, "remindAt must not be null");
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    /**
     * Whether this reminder should fire by the given moment.
     *
     * <p>A reminder is due when it has not been sent and its time is now or in
     * the past. The boundary (exactly at {@code remindAt}) counts as due — a
     * reminder set for 09:00 should fire at 09:00, not a tick later.
     *
     * @param now the reference time, usually {@code LocalDateTime.now()}
     * @return {@code true} if the reminder is pending and its time has come
     */
    public boolean isDue(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");
        return !sent && !remindAt.isAfter(now);
    }

    /** Marks the reminder as delivered so it is not fired again. */
    public void markSent() {
        this.sent = true;
    }

    @Override
    public String toString() {
        return "Reminder{id=%d, taskId=%d, remindAt=%s, sent=%s}"
                .formatted(id, taskId, remindAt, sent);
    }
}
