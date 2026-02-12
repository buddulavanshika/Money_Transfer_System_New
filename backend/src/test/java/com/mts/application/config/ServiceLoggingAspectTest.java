package com.mts.application.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceLoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @InjectMocks
    private ServiceLoggingAspect aspect;

    @BeforeEach
    void setUp() throws Throwable {
        when(joinPoint.getTarget()).thenReturn(this);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn((Class) ServiceLoggingAspectTest.class);
        when(signature.getName()).thenReturn("testMethod");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", 123});
    }

    @Test
    @DisplayName("Aspect logs method execution and returns result successfully")
    void logServiceMethod_success() throws Throwable {
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object result = aspect.logServiceMethod(joinPoint);

        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("Aspect logs method execution time")
    void logServiceMethod_logsExecutionTime() throws Throwable {
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);

        aspect.logServiceMethod(joinPoint);

        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("Aspect logs exception and rethrows it")
    void logServiceMethod_exception() throws Throwable {
        RuntimeException exception = new RuntimeException("Test exception");
        when(joinPoint.proceed()).thenThrow(exception);

        assertThrows(RuntimeException.class, () -> {
            aspect.logServiceMethod(joinPoint);
        });

        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("Aspect handles null arguments")
    void logServiceMethod_nullArguments() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{null, "arg2"});
        Object expectedResult = "test result";
        when(joinPoint.proceed()).thenReturn(expectedResult);

        Object result = aspect.logServiceMethod(joinPoint);

        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
    }
}

