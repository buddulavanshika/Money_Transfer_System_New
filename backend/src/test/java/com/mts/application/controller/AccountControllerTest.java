package com.mts.application.controller;

import com.mts.application.entities.Account;
import com.mts.application.service.AccountService;
import com.mts.domain.enums.AccountStatus;
import com.mts.domain.exceptions.AccountNotFoundException;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(AccountControllerTest.TestSecurityConfig.class)
class AccountControllerTest {

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
    private AccountService accountService;

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns account details")
    void getAccount_success() throws Exception {
        String accountId = "1";
        Account account = Account.builder()
                .id(1L)
                .holderName("Test User")
                .balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE)
                .version(0L)
                .lastUpdated(Instant.now())
                .build();

        when(accountService.getAccountById(accountId)).thenReturn(account);

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId)
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.holderName").value("Test User"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id} returns 404 when account not found")
    void getAccount_notFound() throws Exception {
        String accountId = "999";
        when(accountService.getAccountById(accountId))
                .thenThrow(new AccountNotFoundException("Account not found: " + accountId));

        mockMvc.perform(get("/api/v1/accounts/{id}", accountId)
                .with(user("testuser").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id}/balance returns account balance")
    void getBalance_success() throws Exception {
        String accountId = "1";
        BigDecimal balance = new BigDecimal("1000.00");

        when(accountService.getBalance(accountId)).thenReturn(balance);

        mockMvc.perform(get("/api/v1/accounts/{id}/balance", accountId)
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1000.00));
    }

    @Test
    @DisplayName("GET /api/v1/accounts/{id}/balance returns 404 when account not found")
    void getBalance_notFound() throws Exception {
        String accountId = "999";
        when(accountService.getBalance(accountId))
                .thenThrow(new AccountNotFoundException("Account not found: " + accountId));

        mockMvc.perform(get("/api/v1/accounts/{id}/balance", accountId)
                .with(user("testuser").roles("USER")))
                .andExpect(status().isNotFound());
    }
}

