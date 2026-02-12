package com.mts.domain.exceptions.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainExceptionTest {

    @Test
    @DisplayName("Create DomainException with message")
    void constructor_withMessage() {
        String message = "Domain error occurred";
        DomainException exception = new DomainException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Create DomainException with message and cause")
    void constructor_withMessageAndCause() {
        String message = "Domain error occurred";
        Throwable cause = new RuntimeException("Underlying cause");
        DomainException exception = new DomainException(message, cause);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("DomainException is an Exception")
    void isException() {
        DomainException exception = new DomainException("Test");
        assertTrue(exception instanceof Exception);
    }
}

