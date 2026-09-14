package com.studyflow.service;

import com.studyflow.dao.SubtaskDao;
import com.studyflow.model.Subtask;

import java.util.List;
import java.util.Objects;

/**
 * Business logic for subtasks — the checklist items inside a task.
 *
 * <p>Besides plain CRUD, this layer owns the small but genuinely useful rule of
 * {@link #completionRate(int)}: the share of a task's checklist that is done,
 * which is what turns a checklist into a progress bar.
 */
public class SubtaskService {

    private final SubtaskDao subtaskDao;

    public SubtaskService(SubtaskDao subtaskDao) {
        this.subtaskDao = Objects.requireNonNull(subtaskDao, "subtaskDao must not be null");
    }

    /** Adds a new, unfinished subtask to a task. */
    public Subtask addSubtask(int taskId, String title) {
        return subtaskDao.create(new Subtask(taskId, title));
    }

    public List<Subtask> findByTask(int taskId) {
        return subtaskDao.findByTaskId(taskId);
    }

    /**
     * Flips a subtask between done and not-done.
     *
     * @return {@code true} if the subtask existed and was updated
     */
    public boolean toggle(int id) {
        return subtaskDao.findById(id)
                .map(subtask -> {
                    subtask.toggle();
                    return subtaskDao.update(subtask);
                })
                .orElse(false);
    }

    public boolean updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "subtask must not be null");
        return subtaskDao.update(subtask);
    }

    public boolean deleteSubtask(int id) {
        return subtaskDao.deleteById(id);
    }

    /**
     * Share of a task's subtasks that are done, between 0.0 and 1.0.
     *
     * <p>A task with no subtasks returns 0.0 rather than dividing by zero — an
     * empty checklist has made no progress, which is the honest answer for a
     * progress bar.
     */
    public double completionRate(int taskId) {
        List<Subtask> subtasks = subtaskDao.findByTaskId(taskId);
        if (subtasks.isEmpty()) {
            return 0.0;
        }

        long done = subtasks.stream().filter(Subtask::isDone).count();
        return (double) done / subtasks.size();
    }
}
