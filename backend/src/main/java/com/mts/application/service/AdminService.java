package com.mts.application.service;

import com.mts.application.dto.AccountCreateRequest;
import com.mts.application.dto.AccountResponse;
import com.mts.application.dto.AccountUpdateRequest;
import com.mts.application.dto.TransactionFilter;
import com.mts.application.dto.TransactionResponse;
import com.mts.application.dto.UserCreateRequest;
import com.mts.application.dto.UserResponse;
import com.mts.application.dto.UserUpdateRequest;
import com.mts.application.entities.Account;
import com.mts.application.entities.GlobalConfig;
import com.mts.application.entities.TransactionLog;
import com.mts.application.entities.TransferAuthorization;
import com.mts.application.entities.UserEntity;
import com.mts.application.repository.AccountRepository;
import com.mts.application.repository.GlobalConfigRepository;
import com.mts.application.repository.TransactionLogRepository;
import com.mts.application.repository.TransferAuthorizationRepository;
import com.mts.application.repository.UserRepository;
import com.mts.domain.enums.AccountStatus;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final GlobalConfigRepository globalConfigRepository;
    private final TransferAuthorizationRepository transferAuthorizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.mts.application.service.TransferService transferService;

    public AdminService(AccountRepository accountRepository,
            TransactionLogRepository transactionLogRepository,
            GlobalConfigRepository globalConfigRepository,
            TransferAuthorizationRepository transferAuthorizationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            com.mts.application.service.TransferService transferService) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.globalConfigRepository = globalConfigRepository;
        this.transferAuthorizationRepository = transferAuthorizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transferService = transferService;
    }

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest req) {
        Account account = new Account();
        account.setHolderName(req.holderName());
        account.setBalance(req.openingBalance());
        account.setStatus(AccountStatus.ACTIVE);

        account = accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse updateAccount(String id, AccountUpdateRequest req) {
        Long accountId = parseId(id);
        Account account = getAccountOrThrowWrapped(accountId);

        if (req.holderName() != null) {
            account.setHolderName(req.holderName());
        }

        if (req.dailyLimit() != null) {
            account.setDailyLimit(req.dailyLimit());
        }

        accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public AccountResponse changeAccountStatus(String id, AccountStatus newStatus) {
        Long accountId = parseId(id);
        Account account = getAccountOrThrowWrapped(accountId);
        account.setStatus(newStatus);
        accountRepository.save(account);
        return mapToResponse(account);
    }

    @Transactional
    public void deleteAccount(String id) {
        Long accountId = parseId(id);
        Account account = getAccountOrThrowWrapped(accountId);
        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    public Page<TransactionResponse> searchTransactions(TransactionFilter filter, Pageable pageable) {
        Specification<TransactionLog> spec = Specification.where(null);

        if (filter.accountId() != null) {
            try {
                Long accId = Long.parseLong(filter.accountId());
                spec = spec.and((root, query, cb) -> cb.or(
                        cb.equal(root.get("fromAccountId"), accId),
                        cb.equal(root.get("toAccountId"), accId)));
            } catch (NumberFormatException e) {
                // Ignore invalid ID format in filter
            }
        }

        if (filter.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.status()));
        }

        return transactionLogRepository.findAll(spec, pageable)
                .map(this::mapToTransactionResponse);
    }

    @Transactional
    public void reverseTransaction(String transactionId, String reason) {
        if (!transactionLogRepository.existsById(transactionId)) {
            throw new IllegalArgumentException("Transaction not found");
        }
        // Reversal logic placeholder
    }

    @Transactional
    public void setGlobalTransferLimit(BigDecimal limit) {
        GlobalConfig config = globalConfigRepository.findById("DEFAULT")
                .orElse(new GlobalConfig());
        config.setId("DEFAULT"); // Ensure ID is set if new
        config.setGlobalTransferLimit(limit);
        globalConfigRepository.save(config);
    }

    public BigDecimal getGlobalTransferLimit() {
        return globalConfigRepository.findById("DEFAULT")
                .map(GlobalConfig::getGlobalTransferLimit)
                .orElse(null);
    }

    @Transactional
    public void approveTransfer(Long authorizationId) {
        TransferAuthorization auth = transferAuthorizationRepository.findById(authorizationId)
                .orElseThrow(() -> new IllegalArgumentException("Authorization request not found"));

        if (!"PENDING".equals(auth.getStatus())) {
            throw new IllegalStateException("Request is not in PENDING state");
        }

        auth.setStatus("APPROVED");
        transferAuthorizationRepository.save(auth);

        // Execute the transfer
        com.mts.domain.dto.TransferRequest req = new com.mts.domain.dto.TransferRequest();
        req.setSourceAccountId(auth.getFromAccountId());
        req.setDestinationAccountId(auth.getToAccountId());
        req.setAmount(auth.getAmount());
        req.setCurrency("USD"); // Default
        req.setIdempotencyKey(UUID.randomUUID().toString());

        try {
            transferService.transfer(req);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute approved transfer: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void rejectTransfer(Long authorizationId, String reason) {
        TransferAuthorization auth = transferAuthorizationRepository.findById(authorizationId)
                .orElseThrow(() -> new IllegalArgumentException("Authorization request not found"));

        if (!"PENDING".equals(auth.getStatus())) {
            throw new IllegalStateException("Request is not in PENDING state");
        }

        auth.setStatus("REJECTED");
        auth.setRejectionReason(reason);
        transferAuthorizationRepository.save(auth);
    }

    public java.util.List<TransferAuthorization> getPendingAuthorizations() {
        return transferAuthorizationRepository.findByStatus("PENDING");
    }

    // Wrapped to catch checked Exception and rethrow as Runtime
    // (IllegalArgumentException)
    private Account getAccountOrThrowWrapped(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
    }

    private Long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Account ID format");
        }
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                String.valueOf(account.getId()),
                account.getHolderName(),
                account.getBalance(),
                account.getStatus(),
                account.getDailyLimit(),
                account.getLastUpdated());
    }

    private TransactionResponse mapToTransactionResponse(TransactionLog tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getIdempotencyKey(),
                tx.getFromAccountId(),
                tx.getToAccountId(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getStatus(),
                tx.getFailureReason(),
                java.time.LocalDateTime.ofInstant(tx.getCreatedOn(), java.time.ZoneId.systemDefault()));
    }

    // ==================== User Management Methods ====================

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setEnabled(true);

        // Set roles, default to USER if not specified
        Set<String> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add("USER");
        }
        user.setRoles(roles);

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(request.getRoles());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Soft delete by disabling the user
        user.setEnabled(false);
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles(),
                user.isEnabled());
    }
}
