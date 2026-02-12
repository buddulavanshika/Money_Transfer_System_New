package com.mts.application.security.jwt;

import com.mts.application.security.services.UserDetailsImpl;
import com.mts.application.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private String validToken;
    private String validSecret;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Generate a valid base64-encoded secret key
        byte[] keyBytes = new byte[64];
        for (int i = 0; i < 64; i++) {
            keyBytes[i] = (byte) i;
        }
        validSecret = Base64.getEncoder().encodeToString(keyBytes);
        
        // Create a mock token
        validToken = "Bearer valid.jwt.token";
    }

    @Test
    @DisplayName("Filter processes valid JWT token and sets authentication")
    void doFilterInternal_validToken() throws Exception {
        String jwt = "valid.jwt.token";
        String username = "testuser";

        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                username,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtils.validateJwtToken(jwt)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils).validateJwtToken(jwt);
        verify(jwtUtils).getUserNameFromJwtToken(jwt);
        verify(userDetailsService).loadUserByUsername(username);
        verify(filterChain).doFilter(request, response);
        
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Filter continues without authentication when no token provided")
    void doFilterInternal_noToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateJwtToken(any());
        verify(filterChain).doFilter(request, response);
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Filter continues without authentication when token is invalid")
    void doFilterInternal_invalidToken() throws Exception {
        String jwt = "invalid.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtils.validateJwtToken(jwt)).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils).validateJwtToken(jwt);
        verify(jwtUtils, never()).getUserNameFromJwtToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
        verify(filterChain).doFilter(request, response);
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Filter handles exception gracefully")
    void doFilterInternal_exception() throws Exception {
        String jwt = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtils.validateJwtToken(jwt)).thenThrow(new RuntimeException("Test exception"));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Filter ignores non-Bearer authorization header")
    void doFilterInternal_nonBearerHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateJwtToken(any());
        verify(filterChain).doFilter(request, response);
    }
}

