package com.mts.application.security.services;

import com.mts.application.entities.UserEntity;
import com.mts.application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setPassword("encodedPassword");
        userEntity.setEnabled(true);
        userEntity.setFullName("Test User");
        userEntity.setEmail("test@example.com");
        userEntity.setRoles(Set.of("USER"));
    }

    @Test
    @DisplayName("Load user by username returns UserDetails when user exists")
    void loadUserByUsername_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Load user by username throws exception when user not found")
    void loadUserByUsername_userNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    @DisplayName("Load user with ADMIN role returns correct authorities")
    void loadUserByUsername_adminRole() {
        userEntity.setRoles(Set.of("ADMIN"));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Load user with multiple roles returns all authorities")
    void loadUserByUsername_multipleRoles() {
        userEntity.setRoles(Set.of("USER", "ADMIN"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertEquals(2, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Load user with no roles defaults to ROLE_USER")
    void loadUserByUsername_noRoles() {
        userEntity.setRoles(Set.of());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}

