package com.mts.application.controller;

import com.mts.application.dto.AccountResponse;
import com.mts.application.dto.UserProfileResponse;
import com.mts.application.service.UserService;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestSecurityConfig.class)
class UserControllerTest {

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
    private UserService userService;

    @MockitoBean
    private com.mts.application.security.jwt.JwtUtils jwtUtils;

    @MockitoBean
    private com.mts.application.security.services.UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("GET /api/v1/user/profile returns user profile")
    void getProfile_success() throws Exception {
        UserProfileResponse profile = new UserProfileResponse(1L, "testuser", "Test User",
                "test@example.com", Set.of("USER"));

        when(userService.getMyProfile()).thenReturn(profile);

        mockMvc.perform(get("/api/v1/user/profile")
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    @DisplayName("GET /api/v1/user/accounts returns user accounts")
    void getMyAccounts_success() throws Exception {
        AccountResponse account = new AccountResponse("1", "testuser", new BigDecimal("1000.00"),
                AccountStatus.ACTIVE, null, null);

        when(userService.getMyAccounts()).thenReturn(List.of(account));

        mockMvc.perform(get("/api/v1/user/accounts")
                .with(user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].holderName").value("testuser"))
                .andExpect(jsonPath("$[0].balance").value(1000.00));
    }
}
