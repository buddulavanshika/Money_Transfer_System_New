package com.mts.application.service;

import com.mts.application.dto.AccountCreateRequest;
import com.mts.application.dto.AccountResponse;
import com.mts.application.entities.Account;
import com.mts.application.entities.TransferAuthorization;
import com.mts.application.repository.AccountRepository;
import com.mts.application.repository.GlobalConfigRepository;
import com.mts.application.repository.TransactionLogRepository;
import com.mts.application.repository.TransferAuthorizationRepository;
import com.mts.domain.enums.AccountStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionLogRepository transactionLogRepository;
    @Mock
    private GlobalConfigRepository globalConfigRepository;
    @Mock
    private TransferAuthorizationRepository transferAuthorizationRepository;
    @Mock
    private TransferService transferService;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("Create account saves and returns account response")
    void createAccount_success() {
        AccountCreateRequest req = new AccountCreateRequest("John Doe", new BigDecimal("1000.00"));

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account acc = inv.getArgument(0);
            acc.setId(1L);
            return acc;
        });

        AccountResponse resp = adminService.createAccount(req);

        assertThat(resp).isNotNull();
        assertThat(resp.holderName()).isEqualTo("John Doe");
        assertThat(resp.balance()).isEqualByComparingTo("1000.00");
        assertThat(resp.status()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("Approve transfer executes transfer via TransferService")
    void approveTransfer_success() throws Exception {
        Long authId = 100L;
        TransferAuthorization auth = new TransferAuthorization();
        auth.setId(authId);
        auth.setStatus("PENDING");
        auth.setFromAccountId("1");
        auth.setToAccountId("2");
        auth.setAmount(new BigDecimal("500.00"));

        when(transferAuthorizationRepository.findById(authId)).thenReturn(Optional.of(auth));

        // Act
        adminService.approveTransfer(authId);

        // Assert
        assertThat(auth.getStatus()).isEqualTo("APPROVED");
        verify(transferAuthorizationRepository).save(auth);
        verify(transferService).transfer(argThat(req -> req.getSourceAccountId().equals("1") &&
                req.getDestinationAccountId().equals("2") &&
                req.getAmount().compareTo(new BigDecimal("500.00")) == 0));
    }
}
