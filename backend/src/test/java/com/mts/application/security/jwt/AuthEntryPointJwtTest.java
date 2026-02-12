package com.mts.application.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private AuthEntryPointJwt authEntryPointJwt;

    private ByteArrayOutputStream outputStream;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        outputStream = new ByteArrayOutputStream();
        printWriter = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
        
        when(response.getOutputStream()).thenReturn(
                new jakarta.servlet.ServletOutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        outputStream.write(b);
                    }
                }
        );
    }

    @Test
    @DisplayName("Commence sets correct response status and content type")
    void commence_setsResponseProperties() throws Exception {
        when(request.getServletPath()).thenReturn("/api/test");
        when(authException.getMessage()).thenReturn("Unauthorized");

        authEntryPointJwt.commence(request, response, authException);

        verify(response).setContentType("application/json");
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Commence writes correct error response body")
    void commence_writesErrorResponse() throws Exception {
        when(request.getServletPath()).thenReturn("/api/test");
        when(authException.getMessage()).thenReturn("Unauthorized");

        // Create a mock output stream that we can verify
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jakarta.servlet.ServletOutputStream servletOutputStream = new jakarta.servlet.ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }
        };
        
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        authEntryPointJwt.commence(request, response, authException);

        String responseBody = baos.toString(StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        var jsonNode = mapper.readTree(responseBody);

        assertEquals(401, jsonNode.get("status").asInt());
        assertEquals("Unauthorized", jsonNode.get("error").asText());
        assertEquals("/api/test", jsonNode.get("path").asText());
    }
}

