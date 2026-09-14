package com.studyflow.service;

import com.studyflow.dao.TaskDao;
import com.studyflow.model.Task;
import com.studyflow.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TaskService} using a mocked DAO.
 *
 * <p>These never touch a database, so they run in milliseconds and fail only
 * when the business rules themselves are wrong — not when a database is
 * unavailable or holds unexpected data.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService")
class TaskServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 14, 12, 0);

    @Mock
    private TaskDao taskDao;

    private TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService(taskDao);
    }

    private static Task task(int id, LocalDateTime deadline, TaskStatus status) {
        return new Task(id, 1, "Task " + id, null, deadline, status);
    }

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("stores a new task as PENDING")
        void createsPendingTask() {
            when(taskDao.create(any(Task.class))).thenAnswer(call -> call.getArgument(0));

            service.createTask(1, "New task", "desc", NOW.plusDays(1), NOW);

            ArgumentCaptor<Task> captured = ArgumentCaptor.forClass(Task.class);
            verify(taskDao).create(captured.capture());

            assertAll(
                    () -> assertEquals("New task", captured.getValue().getTitle()),
                    () -> assertEquals(TaskStatus.PENDING, captured.getValue().getStatus())
            );
        }

        @Test
        @DisplayName("accepts a task with no deadline")
        void acceptsNullDeadline() {
            when(taskDao.create(any(Task.class))).thenAnswer(call -> call.getArgument(0));

            service.createTask(1, "Someday", null, null, NOW);

            verify(taskDao).create(any(Task.class));
        }

        @Test
        @DisplayName("refuses a deadline in the past and never touches the database")
        void rejectsPastDeadline() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.createTask(1, "Too late", null, NOW.minusDays(1), NOW));

            verify(taskDao, never()).create(any(Task.class));
        }
    }

    @Nested
    @DisplayName("saveNew")
    class SaveNew {

        @Test
        @DisplayName("persists a fully-specified task as-is, keeping its status")
        void persistsAsIs() {
            when(taskDao.create(any(Task.class))).thenAnswer(call -> call.getArgument(0));

            Task saved = service.saveNew(
                    new Task(1, "Already started", "desc", NOW.plusDays(1), TaskStatus.IN_PROGRESS));

            assertAll(
                    () -> assertEquals(TaskStatus.IN_PROGRESS, saved.getStatus()),
                    () -> verify(taskDao).create(any(Task.class))
            );
        }
    }

    @Nested
    @DisplayName("findOverdue")
    class FindOverdue {

        @Test
        @DisplayName("returns only unfinished tasks past their deadline")
        void returnsOnlyOverdue() {
            when(taskDao.findAll()).thenReturn(List.of(
                    task(1, NOW.minusDays(2), TaskStatus.PENDING),
                    task(2, NOW.plusDays(2), TaskStatus.PENDING),
                    task(3, NOW.minusDays(5), TaskStatus.DONE),
                    task(4, null, TaskStatus.PENDING)
            ));

            List<Task> overdue = service.findOverdue(NOW);

            assertAll(
                    () -> assertEquals(1, overdue.size()),
                    () -> assertEquals(1, overdue.get(0).getId())
            );
        }

        @Test
        @DisplayName("sorts the most overdue task first")
        void sortsByDeadline() {
            when(taskDao.findAll()).thenReturn(List.of(
                    task(1, NOW.minusDays(1), TaskStatus.PENDING),
                    task(2, NOW.minusDays(10), TaskStatus.PENDING)
            ));

            assertEquals(2, service.findOverdue(NOW).get(0).getId());
        }

        @Test
        @DisplayName("returns an empty list when nothing is overdue")
        void emptyWhenNothingOverdue() {
            when(taskDao.findAll()).thenReturn(List.of(
                    task(1, NOW.plusDays(1), TaskStatus.PENDING)
            ));

            assertTrue(service.findOverdue(NOW).isEmpty());
        }
    }

    @Nested
    @DisplayName("findUpcoming")
    class FindUpcoming {

        @Test
        @DisplayName("returns unfinished tasks inside the window")
        void returnsTasksInWindow() {
            when(taskDao.findAll()).thenReturn(List.of(
                    task(1, NOW.plusDays(2), TaskStatus.PENDING),
                    task(2, NOW.plusDays(30), TaskStatus.PENDING),
                    task(3, NOW.minusDays(1), TaskStatus.PENDING),
                    task(4, NOW.plusDays(3), TaskStatus.DONE)
            ));

            List<Task> upcoming = service.findUpcoming(NOW, 7);

            assertAll(
                    () -> assertEquals(1, upcoming.size()),
                    () -> assertEquals(1, upcoming.get(0).getId())
            );
        }

        @Test
        @DisplayName("includes a task falling exactly on the boundary")
        void includesBoundary() {
            when(taskDao.findAll()).thenReturn(List.of(
                    task(1, NOW.plusDays(7), TaskStatus.PENDING)
            ));

            assertEquals(1, service.findUpcoming(NOW, 7).size());
        }

        @Test
        @DisplayName("rejects a non-positive window")
        void rejectsInvalidWindow() {
            assertThrows(IllegalArgumentException.class, () -> service.findUpcoming(NOW, 0));
        }
    }

    @Nested
    @DisplayName("completeTask")
    class CompleteTask {

        @Test
        @DisplayName("marks an existing task as DONE and saves it")
        void marksExistingTaskDone() {
            Task pending = task(1, NOW.plusDays(1), TaskStatus.PENDING);
            when(taskDao.findById(1)).thenReturn(Optional.of(pending));
            when(taskDao.update(pending)).thenReturn(true);

            boolean result = service.completeTask(1);

            assertAll(
                    () -> assertTrue(result),
                    () -> assertEquals(TaskStatus.DONE, pending.getStatus())
            );
        }

        @Test
        @DisplayName("reports false and saves nothing when the task is missing")
        void falseForMissingTask() {
            when(taskDao.findById(99)).thenReturn(Optional.empty());

            assertFalse(service.completeTask(99));
            verify(taskDao, never()).update(any(Task.class));
        }
    }

    @Nested
    @DisplayName("completionRate")
    class CompletionRate {

        @Test
        @DisplayName("returns the share of finished tasks")
        void calculatesShare() {
            when(taskDao.findAll()).thenReturn(List.of(
                    task(1, null, TaskStatus.DONE),
                    task(2, null, TaskStatus.DONE),
                    task(3, null, TaskStatus.PENDING),
                    task(4, null, TaskStatus.IN_PROGRESS)
            ));

            assertEquals(0.5, service.completionRate(), 0.0001);
        }

        @Test
        @DisplayName("returns zero instead of dividing by zero on an empty planner")
        void zeroWhenNoTasks() {
            when(taskDao.findAll()).thenReturn(List.of());

            assertEquals(0.0, service.completionRate(), 0.0001);
        }

        @Test
        @DisplayName("returns one when everything is finished")
        void oneWhenAllDone() {
            when(taskDao.findAll()).thenReturn(List.of(
                    task(1, null, TaskStatus.DONE)
            ));

            assertEquals(1.0, service.completionRate(), 0.0001);
        }
    }
}
