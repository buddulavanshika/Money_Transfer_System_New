package com.mts.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.application.dto.TransferApprovalRequest;
import com.mts.application.service.AdminService;
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

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferApprovalController.class)
@Import(TransferApprovalControllerTest.TestSecurityConfig.class)
class TransferApprovalControllerTest {

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

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private com.mts.application.security.jwt.JwtUtils jwtUtils;

    @MockitoBean
    private com.mts.application.security.services.UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/admin/transfers/approvals/{id} approves transfer")
    void processApproval_approve_success() throws Exception {
        TransferApprovalRequest request = new TransferApprovalRequest(true, null);

        mockMvc.perform(post("/api/v1/admin/transfers/approvals/1")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(adminService).approveTransfer(1L);
    }

    @Test
    @DisplayName("POST /api/v1/admin/transfers/approvals/{id} rejects transfer")
    void processApproval_reject_success() throws Exception {
        TransferApprovalRequest request = new TransferApprovalRequest(false, "Insufficient documentation");

        mockMvc.perform(post("/api/v1/admin/transfers/approvals/1")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(adminService).rejectTransfer(1L, "Insufficient documentation");
    }
}
