package com.mts.domain.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptimisticLockExceptionTest {

    @Test
    @DisplayName("Create OptimisticLockException with message")
    void constructor_withMessage() {
        String message = "Concurrent modification detected";
        OptimisticLockException exception = new OptimisticLockException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Create OptimisticLockException with message and cause")
    void constructor_withMessageAndCause() {
        String message = "Concurrent modification detected";
        Throwable cause = new RuntimeException("Underlying cause");
        OptimisticLockException exception = new OptimisticLockException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("OptimisticLockException is an Exception")
    void isException() {
        OptimisticLockException exception = new OptimisticLockException("Test");
        assertTrue(exception instanceof Exception);
    }
}

