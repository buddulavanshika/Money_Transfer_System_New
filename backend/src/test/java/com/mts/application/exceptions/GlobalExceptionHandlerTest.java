package com.mts.application.exceptions;

import com.mts.domain.dto.ErrorResponse;
import com.mts.domain.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Handle AccountNotFoundException returns 404")
    void handleAccountNotFound() {
        AccountNotFoundException ex = new AccountNotFoundException("Account not found: 123");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCOUNT_NOT_FOUND", response.getBody().code());
        assertEquals("Account not found: 123", response.getBody().message());
    }

    @Test
    @DisplayName("Handle InsufficientBalanceException returns 400")
    void handleInsufficientBalance() {
        InsufficientBalanceException ex = new InsufficientBalanceException("Insufficient balance");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsufficientBalance(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INSUFFICIENT_BALANCE", response.getBody().code());
    }

    @Test
    @DisplayName("Handle AccountNotActiveException returns 403")
    void handleAccountNotActive() {
        AccountNotActiveException ex = new AccountNotActiveException("Account is not active");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountNotActive(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCOUNT_NOT_ACTIVE", response.getBody().code());
    }

    @Test
    @DisplayName("Handle DuplicateTransferException returns 409")
    void handleDuplicateTransfer() {
        DuplicateTransferException ex = new DuplicateTransferException("Duplicate transfer");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateTransfer(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DUPLICATE_TRANSFER", response.getBody().code());
    }

    @Test
    @DisplayName("Handle OptimisticLockException returns 409")
    void handleOptimisticLock() {
        OptimisticLockException ex = new OptimisticLockException("Concurrent modification detected");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOptimisticLock(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CONCURRENT_MODIFICATION", response.getBody().code());
    }

    @Test
    @DisplayName("Handle DataIntegrityViolationException returns 409")
    void handleDuplicateKey() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate key");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateKey(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DUPLICATE_REQUEST", response.getBody().code());
        assertNotNull(response.getBody().details());
    }

    @Test
    @DisplayName("Handle MethodArgumentNotValidException returns 422")
    void handleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("object", "field", "error message");
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        assertEquals(422, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_FAILED", response.getBody().code());
        assertNotNull(response.getBody().details());
    }

    @Test
    @DisplayName("Handle ConstraintViolationException returns 422")
    void handleConstraintViolation() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("error message");
        violations.add(violation);
        when(ex.getConstraintViolations()).thenReturn(violations);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolation(ex, request);

        assertEquals(422, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("CONSTRAINT_VIOLATION", response.getBody().code());
    }

    @Test
    @DisplayName("Handle IllegalArgumentException returns 400")
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_REQUEST", response.getBody().code());
    }

    @Test
    @DisplayName("Handle MethodArgumentTypeMismatchException returns 400")
    void handleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) Long.class);
        when(ex.getValue()).thenReturn("invalid");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatch(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TYPE_MISMATCH", response.getBody().code());
    }

    @Test
    @DisplayName("Handle HttpMessageNotReadableException returns 400")
    void handleNotReadable() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotReadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MALFORMED_REQUEST", response.getBody().code());
    }

    @Test
    @DisplayName("Handle generic Exception returns 500")
    void handleGeneric() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
    }
}

