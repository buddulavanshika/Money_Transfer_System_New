package com.mts.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mts.application.service.TransferService;
import com.mts.domain.dto.TransferRequest;
import com.mts.domain.dto.TransferResponse;
import com.mts.domain.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.context.TestConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@WebMvcTest(value = TransferController.class)
@Import(TransferControllerTest.TestConfig.class)
class TransferControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    static class TestConfig {

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/**").hasRole("USER")
                            .anyRequest().permitAll())
                    .exceptionHandling(eh -> eh
                            .authenticationEntryPoint((HttpServletRequest request, HttpServletResponse response,
                                    org.springframework.security.core.AuthenticationException authException) -> {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            }));
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    private TransferService transferService;
    @MockitoBean
    private com.mts.application.security.jwt.JwtUtils jwtUtils;
    @MockitoBean
    private com.mts.application.security.services.UserDetailsServiceImpl userDetailsService;

    // ... (rest of tests)

    @Test
    @DisplayName("POST /api/v1/transfers without auth returns 401")
    void executeTransfer_withoutAuth_returns401() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId("1");
        request.setDestinationAccountId("2");
        request.setAmount(new BigDecimal("100.00"));
        request.setIdempotencyKey("key-1");

        mockMvc.perform(post("/api/v1/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/transfers with valid request returns 200 and TransferResponse")
    void executeTransfer_withAuth_success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId("1");
        request.setDestinationAccountId("2");
        request.setAmount(new BigDecimal("100.00"));
        request.setIdempotencyKey("key-1");

        TransferResponse response = new TransferResponse(
                "tx-123", "1", "2", new BigDecimal("100.00"), "USD",
                TransactionStatus.SUCCESS, "Transfer completed successfully", "key-1", Instant.now());
        when(transferService.transfer(any(TransferRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/transfers")
                .with(user("testuser").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("tx-123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /api/v1/transfers with invalid body (missing amount) returns 422")
    void executeTransfer_validationFailure_returns422() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setSourceAccountId("1");
        request.setDestinationAccountId("2");
        request.setAmount(null);
        request.setIdempotencyKey("key-1");

        mockMvc.perform(post("/api/v1/transfers")
                .with(user("testuser").roles("USER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}
