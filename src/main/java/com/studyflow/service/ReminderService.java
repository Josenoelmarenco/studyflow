package com.studyflow.service;

import com.studyflow.dao.ReminderDao;
import com.studyflow.model.Reminder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Business logic for reminders.
 *
 * <p>The interesting rules live here rather than in the DAO: a reminder cannot
 * be scheduled in the past, and "which reminders should fire now" is a single
 * question the UI (or, later, a background scheduler) can ask.
 */
public class ReminderService {

    private final ReminderDao reminderDao;

    public ReminderService(ReminderDao reminderDao) {
        this.reminderDao = Objects.requireNonNull(reminderDao, "reminderDao must not be null");
    }

    /**
     * Schedules a reminder for a task.
     *
     * @param taskId   the task to remind about
     * @param remindAt when the reminder should fire
     * @param now      the current time, injected so the past-check is testable
     * @throws IllegalArgumentException if {@code remindAt} is before {@code now}
     */
    public Reminder schedule(int taskId, LocalDateTime remindAt, LocalDateTime now) {
        Objects.requireNonNull(remindAt, "remindAt must not be null");
        Objects.requireNonNull(now, "now must not be null");

        if (remindAt.isBefore(now)) {
            throw new IllegalArgumentException("Cannot schedule a reminder in the past: " + remindAt);
        }

        return reminderDao.create(new Reminder(taskId, remindAt));
    }

    public List<Reminder> findByTask(int taskId) {
        return reminderDao.findByTaskId(taskId);
    }

    /**
     * The reminders that should fire by {@code now}: pending and past-due.
     *
     * @param now the reference time, injected so the rule is testable
     */
    public List<Reminder> dueReminders(LocalDateTime now) {
        Objects.requireNonNull(now, "now must not be null");
        return reminderDao.findDue(now);
    }

    /**
     * Marks a reminder as delivered so it stops appearing in
     * {@link #dueReminders(LocalDateTime)}.
     *
     * @return {@code true} if the reminder existed and was updated
     */
    public boolean markSent(int id) {
        return reminderDao.findById(id)
                .map(reminder -> {
                    reminder.markSent();
                    return reminderDao.update(reminder);
                })
                .orElse(false);
    }

    public boolean deleteReminder(int id) {
        return reminderDao.deleteById(id);
    }
}
