package com.studyflow.service;

import com.studyflow.dao.SubtaskDao;
import com.studyflow.model.Subtask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubtaskService")
class SubtaskServiceTest {

    @Mock
    private SubtaskDao subtaskDao;

    private SubtaskService service;

    @BeforeEach
    void setUp() {
        service = new SubtaskService(subtaskDao);
    }

    private static Subtask subtask(int id, boolean done) {
        return new Subtask(id, 1, "Subtask " + id, done);
    }

    @Nested
    @DisplayName("addSubtask")
    class AddSubtask {

        @Test
        @DisplayName("stores a new subtask under the task")
        void storesSubtask() {
            when(subtaskDao.create(any(Subtask.class))).thenAnswer(call -> call.getArgument(0));

            service.addSubtask(1, "Collect team hours");

            verify(subtaskDao).create(any(Subtask.class));
        }
    }

    @Nested
    @DisplayName("toggle")
    class Toggle {

        @Test
        @DisplayName("flips an existing subtask and saves it")
        void togglesExisting() {
            Subtask s = subtask(1, false);
            when(subtaskDao.findById(1)).thenReturn(Optional.of(s));
            when(subtaskDao.update(s)).thenReturn(true);

            boolean result = service.toggle(1);

            assertAll(
                    () -> assertTrue(result),
                    () -> assertTrue(s.isDone())
            );
        }

        @Test
        @DisplayName("reports false and saves nothing when the subtask is missing")
        void falseForMissing() {
            when(subtaskDao.findById(99)).thenReturn(Optional.empty());

            assertFalse(service.toggle(99));
            verify(subtaskDao, never()).update(any(Subtask.class));
        }
    }

    @Nested
    @DisplayName("completionRate")
    class CompletionRate {

        @Test
        @DisplayName("returns the share of done subtasks")
        void calculatesShare() {
            when(subtaskDao.findByTaskId(1)).thenReturn(List.of(
                    subtask(1, true),
                    subtask(2, true),
                    subtask(3, false),
                    subtask(4, false)
            ));

            assertEquals(0.5, service.completionRate(1), 0.0001);
        }

        @Test
        @DisplayName("returns zero for a task with no subtasks")
        void zeroWhenEmpty() {
            when(subtaskDao.findByTaskId(1)).thenReturn(List.of());

            assertEquals(0.0, service.completionRate(1), 0.0001);
        }

        @Test
        @DisplayName("returns one when every subtask is done")
        void oneWhenAllDone() {
            when(subtaskDao.findByTaskId(1)).thenReturn(List.of(subtask(1, true)));

            assertEquals(1.0, service.completionRate(1), 0.0001);
        }
    }

    @Nested
    @DisplayName("delegation")
    class Delegation {

        @Test
        @DisplayName("delegates findByTask to the DAO")
        void delegatesFindByTask() {
            when(subtaskDao.findByTaskId(1)).thenReturn(List.of(subtask(1, false)));

            assertEquals(1, service.findByTask(1).size());
        }

        @Test
        @DisplayName("delegates delete to the DAO")
        void delegatesDelete() {
            when(subtaskDao.deleteById(1)).thenReturn(true);

            assertTrue(service.deleteSubtask(1));
        }
    }
}
