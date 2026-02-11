package com.mts.application.service;

import com.mts.application.entities.Account;
import com.mts.application.repository.AccountRepository;
import com.mts.domain.enums.AccountStatus;
import com.mts.domain.exceptions.AccountNotActiveException;
import com.mts.domain.exceptions.AccountNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    @DisplayName("getAccountById returns account when found")
    void getAccountById_success() throws Exception {
        Account account = new Account();
        account.setId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Account result = accountService.getAccountById("1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAccountById throws AccountNotFoundException when not found")
    void getAccountById_notFound_throws() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById("1"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("getAccountById throws AccountNotFoundException for invalid ID format")
    void getAccountById_invalidFormat_throws() {
        assertThatThrownBy(() -> accountService.getAccountById("abc"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("validateAccountForTransfer does nothing if account is active")
    void validateAccountForTransfer_active_success() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.validateAccountForTransfer("1");
    }

    @Test
    @DisplayName("validateAccountForTransfer throws AccountNotActiveException if not active")
    void validateAccountForTransfer_inactive_throws() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.LOCKED);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.validateAccountForTransfer("1"))
                .isInstanceOf(AccountNotActiveException.class);
    }
}
