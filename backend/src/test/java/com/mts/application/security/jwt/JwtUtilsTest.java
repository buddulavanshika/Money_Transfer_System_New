package com.mts.application.security.jwt;

import com.mts.application.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String validSecret;
    private long expiryMinutes = 30;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Generate a valid base64-encoded secret key (512 bits for HS512)
        byte[] keyBytes = new byte[64];
        for (int i = 0; i < 64; i++) {
            keyBytes[i] = (byte) i;
        }
        validSecret = Base64.getEncoder().encodeToString(keyBytes);
        
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", validSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiryMinutes", expiryMinutes);
    }

    @Test
    @DisplayName("Generate JWT token successfully")
    void generateJwtToken_success() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Extract username from valid JWT token")
    void getUserNameFromJwtToken_success() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);
        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Validate valid JWT token returns true")
    void validateJwtToken_validToken() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);
        boolean isValid = jwtUtils.validateJwtToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Validate invalid JWT token returns false")
    void validateJwtToken_invalidToken() {
        String invalidToken = "invalid.token.here";
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate null token returns false")
    void validateJwtToken_nullToken() {
        boolean isValid = jwtUtils.validateJwtToken(null);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate empty token returns false")
    void validateJwtToken_emptyToken() {
        boolean isValid = jwtUtils.validateJwtToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Validate token with wrong secret returns false")
    void validateJwtToken_wrongSecret() {
        // Create token with one secret
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = jwtUtils.generateJwtToken(authentication);

        // Change secret
        byte[] wrongKeyBytes = new byte[64];
        for (int i = 0; i < 64; i++) {
            wrongKeyBytes[i] = (byte) (i + 1);
        }
        String wrongSecret = Base64.getEncoder().encodeToString(wrongKeyBytes);
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", wrongSecret);

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertFalse(isValid);
    }
}

