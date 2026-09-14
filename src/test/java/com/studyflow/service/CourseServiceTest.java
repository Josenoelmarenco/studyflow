package com.studyflow.service;

import com.studyflow.dao.CourseDao;
import com.studyflow.model.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService")
class CourseServiceTest {

    @Mock
    private CourseDao courseDao;

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(courseDao);
    }

    @Nested
    @DisplayName("createCourse")
    class CreateCourse {

        @Test
        @DisplayName("builds a course from the fields and stores it")
        void storesCourse() {
            when(courseDao.create(any(Course.class))).thenAnswer(call -> call.getArgument(0));

            service.createCourse(3, "Software Project", "TX00EY27", "Autumn 2026");

            ArgumentCaptor<Course> captured = ArgumentCaptor.forClass(Course.class);
            verify(courseDao).create(captured.capture());

            assertAll(
                    () -> assertEquals(3, captured.getValue().getUserId()),
                    () -> assertEquals("Software Project", captured.getValue().getName()),
                    () -> assertEquals("TX00EY27", captured.getValue().getCode())
            );
        }

        @Test
        @DisplayName("rejects an invalid course and never touches the database")
        void rejectsInvalid() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.createCourse(3, "  ", "X", "Autumn 2026"));

            verify(courseDao, never()).create(any(Course.class));
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        @DisplayName("delegates findById to the DAO")
        void delegatesFindById() {
            Course course = new Course(1, 1, "C", "X", "Autumn 2026");
            when(courseDao.findById(1)).thenReturn(Optional.of(course));

            assertEquals(Optional.of(course), service.findById(1));
        }

        @Test
        @DisplayName("delegates findAll to the DAO")
        void delegatesFindAll() {
            when(courseDao.findAll()).thenReturn(List.of(new Course(1, 1, "C", "X", "Autumn 2026")));

            assertEquals(1, service.findAll().size());
        }

        @Test
        @DisplayName("delegates findByUser to the DAO")
        void delegatesFindByUser() {
            when(courseDao.findByUserId(5)).thenReturn(List.of());

            assertTrue(service.findByUser(5).isEmpty());
        }
    }

    @Nested
    @DisplayName("update and delete")
    class UpdateDelete {

        @Test
        @DisplayName("delegates update to the DAO")
        void delegatesUpdate() {
            Course course = new Course(1, 1, "C", "X", "Autumn 2026");
            when(courseDao.update(course)).thenReturn(true);

            assertTrue(service.updateCourse(course));
        }

        @Test
        @DisplayName("delegates delete to the DAO")
        void delegatesDelete() {
            when(courseDao.deleteById(1)).thenReturn(true);

            assertTrue(service.deleteCourse(1));
        }
    }
}
