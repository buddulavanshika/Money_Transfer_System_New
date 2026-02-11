package com.mts.application.service;

import com.mts.application.dto.AccountResponse;
import com.mts.application.dto.UserProfileResponse;
import com.mts.application.entities.Account;
import com.mts.application.entities.UserEntity;
import com.mts.application.repository.AccountRepository;
import com.mts.application.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setFullName("Test User");
        userEntity.setEmail("test@example.com");
        userEntity.setRoles(Set.of("USER"));
    }

    private void mockSecurityContext() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("getMyProfile returns correct user profile")
    void getMyProfile_success() {
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        UserProfileResponse response = userService.getMyProfile();

        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.fullName()).isEqualTo("Test User");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.roles()).contains("USER");
    }

    @Test
    @DisplayName("getMyAccounts returns list of accounts for the user")
    void getMyAccounts_success() {
        mockSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        Account account = new Account();
        account.setId(100L);
        account.setHolderName("testuser");
        account.setBalance(new BigDecimal("500.00"));

        when(accountRepository.findByHolderName("testuser")).thenReturn(List.of(account));

        List<AccountResponse> accounts = userService.getMyAccounts();

        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).holderName()).isEqualTo("testuser");
        assertThat(accounts.get(0).balance()).isEqualByComparingTo("500.00");
    }
}
