package com.mts.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.application.dto.AccountCreateRequest;
import com.mts.application.dto.AccountResponse;
import com.mts.application.dto.AccountUpdateRequest;
import com.mts.application.service.AdminService;
import com.mts.domain.enums.AccountStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAccountController.class)
@Import(AdminAccountControllerTest.TestSecurityConfig.class)
class AdminAccountControllerTest {

        @TestConfiguration
        @EnableWebSecurity
        static class TestSecurityConfig {
                @Bean
                @Order(Ordered.HIGHEST_PRECEDENCE)
                public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                        http
                                        .csrf(csrf -> csrf.disable())
                                        .sessionManagement(
                                                        sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
                        return http.build();
                }
        }

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AdminService adminService;

        @MockitoBean
        private com.mts.application.security.jwt.JwtUtils jwtUtils;

        @MockitoBean
        private com.mts.application.security.services.UserDetailsServiceImpl userDetailsService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        @DisplayName("POST /api/v1/admin/accounts creates account")
        void createAccount_success() throws Exception {
                AccountCreateRequest request = new AccountCreateRequest("John Doe", new BigDecimal("1000.00"));
                AccountResponse response = new AccountResponse("1", "John Doe", new BigDecimal("1000.00"),
                                AccountStatus.ACTIVE, new BigDecimal("500.00"), null);

                when(adminService.createAccount(any(AccountCreateRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/admin/accounts")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.holderName").value("John Doe"));

                verify(adminService).createAccount(any(AccountCreateRequest.class));
        }

        @Test
        @DisplayName("PUT /api/v1/admin/accounts/{id} updates account")
        void updateAccount_success() throws Exception {
                AccountUpdateRequest request = new AccountUpdateRequest("John Doe Updated", new BigDecimal("500.00"));
                AccountResponse response = new AccountResponse("1", "John Doe Updated", new BigDecimal("2000.00"),
                                AccountStatus.ACTIVE, new BigDecimal("500.00"), null);

                when(adminService.updateAccount(eq("1"), any(AccountUpdateRequest.class))).thenReturn(response);

                mockMvc.perform(put("/api/v1/admin/accounts/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.dailyLimit").value(500.00));
        }

        @Test
        @DisplayName("PATCH /api/v1/admin/accounts/{id}/status changes status")
        void changeAccountStatus_success() throws Exception {
                AccountResponse response = new AccountResponse("1", "John Doe", new BigDecimal("1000.00"),
                                AccountStatus.LOCKED, null, null);

                when(adminService.changeAccountStatus("1", AccountStatus.LOCKED)).thenReturn(response);

                mockMvc.perform(patch("/api/v1/admin/accounts/1/status")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf())
                                .param("status", "LOCKED"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("LOCKED"));
        }

        @Test
        @DisplayName("DELETE /api/v1/admin/accounts/{id} deletes account")
        void deleteAccount_success() throws Exception {
                mockMvc.perform(delete("/api/v1/admin/accounts/1")
                                .with(user("admin").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isNoContent());

                verify(adminService).deleteAccount("1");
        }
}
