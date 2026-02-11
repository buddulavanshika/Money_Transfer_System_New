package com.mts.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.application.dto.TransactionFilter;
import com.mts.application.dto.TransactionResponse;
import com.mts.application.service.AdminService;
import com.mts.domain.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTransactionController.class)
@Import(AdminTransactionControllerTest.TestSecurityConfig.class)
class AdminTransactionControllerTest {

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
    @DisplayName("GET /api/v1/admin/transactions searches transactions")
    void searchTransactions_success() throws Exception {
        TransactionResponse txResponse = new TransactionResponse("tx-1", "key-1", 1L, 2L,
                new BigDecimal("100.00"), "USD", TransactionStatus.SUCCESS, null, java.time.LocalDateTime.now());
        Page<TransactionResponse> page = new PageImpl<>(Collections.singletonList(txResponse));

        when(adminService.searchTransactions(any(TransactionFilter.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/transactions")
                .with(user("admin").roles("ADMIN"))
                .param("accountId", "1")
                .param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("tx-1"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/transactions/{id}/reverse reverses transaction")
    void reverseTransaction_success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/transactions/tx-1/reverse")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"Fraud detected\""))
                .andExpect(status().isOk());

        verify(adminService).reverseTransaction(eq("tx-1"), any());
    }

    @Test
    @DisplayName("POST /api/v1/admin/transactions/limits/global sets global limit")
    void setGlobalLimit_success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/transactions/limits/global")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("5000.00"))
                .andExpect(status().isOk());

        verify(adminService).setGlobalTransferLimit(any(BigDecimal.class));
    }

    @Test
    @DisplayName("GET /api/v1/admin/transactions/limits/global gets global limit")
    void getGlobalLimit_success() throws Exception {
        when(adminService.getGlobalTransferLimit()).thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/api/v1/admin/transactions/limits/global")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string("5000.00"));
    }
}
