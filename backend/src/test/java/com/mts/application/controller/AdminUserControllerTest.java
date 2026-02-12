package com.mts.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.application.dto.UserCreateRequest;
import com.mts.application.dto.UserResponse;
import com.mts.application.dto.UserUpdateRequest;
import com.mts.application.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import(AdminUserControllerTest.TestSecurityConfig.class)
class AdminUserControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/admin/users creates a new user")
    void createUser_success() throws Exception {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setEmail("newuser@example.com");
        request.setRoles(Set.of("USER"));

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("newuser");
        response.setFullName("New User");
        response.setEmail("newuser@example.com");
        response.setRoles(Set.of("USER"));
        response.setEnabled(true);

        when(adminService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/users")
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.fullName").value("New User"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/users returns all users")
    void getAllUsers_success() throws Exception {
        UserResponse user1 = new UserResponse();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEnabled(true);

        UserResponse user2 = new UserResponse();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEnabled(true);

        when(adminService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/v1/admin/users")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/users/{id} returns user by ID")
    void getUserById_success() throws Exception {
        Long userId = 1L;
        UserResponse response = new UserResponse();
        response.setId(userId);
        response.setUsername("testuser");
        response.setFullName("Test User");
        response.setEnabled(true);

        when(adminService.getUserById(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/users/{id}", userId)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/users/{id} updates user")
    void updateUser_success() throws Exception {
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");
        request.setRoles(Set.of("USER", "ADMIN"));
        request.setEnabled(true);

        UserResponse response = new UserResponse();
        response.setId(userId);
        response.setUsername("testuser");
        response.setFullName("Updated Name");
        response.setEmail("updated@example.com");
        response.setRoles(Set.of("USER", "ADMIN"));
        response.setEnabled(true);

        when(adminService.updateUser(eq(userId), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/admin/users/{id}", userId)
                .with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/users/{id} deletes user")
    void deleteUser_success() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/v1/admin/users/{id}", userId)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }
}

