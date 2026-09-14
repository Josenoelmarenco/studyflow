package com.studyflow.service;

import com.studyflow.dao.UserDao;
import com.studyflow.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserDao userDao;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userDao);
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("stores a new user")
        void storesUser() {
            when(userDao.create(any(User.class))).thenAnswer(call -> call.getArgument(0));

            service.register("José", "jose@studyflow.local", "hash");

            verify(userDao).create(any(User.class));
        }
    }

    @Nested
    @DisplayName("findOrCreateDefault")
    class FindOrCreateDefault {

        @Test
        @DisplayName("returns the existing user without creating a new one")
        void returnsExisting() {
            User existing = new User(1, "José", "jose@studyflow.local", "hash", null);
            when(userDao.findByEmail("jose@studyflow.local")).thenReturn(Optional.of(existing));

            User result = service.findOrCreateDefault("José", "jose@studyflow.local", "hash");

            assertSame(existing, result);
            verify(userDao, never()).create(any(User.class));
        }

        @Test
        @DisplayName("creates a user when none exists yet")
        void createsWhenMissing() {
            when(userDao.findByEmail("jose@studyflow.local")).thenReturn(Optional.empty());
            when(userDao.create(any(User.class))).thenAnswer(call -> call.getArgument(0));

            User result = service.findOrCreateDefault("José", "jose@studyflow.local", "hash");

            assertEquals("jose@studyflow.local", result.getEmail());
            verify(userDao).create(any(User.class));
        }
    }
}
