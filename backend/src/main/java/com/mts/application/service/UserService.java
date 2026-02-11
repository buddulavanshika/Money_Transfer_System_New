package com.mts.application.service;

import com.mts.application.dto.AccountResponse;
import com.mts.application.dto.ChangePasswordRequest;
import com.mts.application.dto.ProfileUpdateRequest;
import com.mts.application.dto.UserProfileResponse;
import com.mts.application.entities.Account;
import com.mts.application.entities.UserEntity;
import com.mts.application.repository.AccountRepository;
import com.mts.application.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        UserEntity user = getCurrentUser();
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles());
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {
        UserEntity user = getCurrentUser();
        // Assuming holderName links to username
        return accountRepository.findByHolderName(user.getUsername()).stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserProfileResponse updateProfile(ProfileUpdateRequest request) {
        UserEntity user = getCurrentUser();

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        user = userRepository.save(user);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        UserEntity user = getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserEntity getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return new AccountResponse(
                String.valueOf(account.getId()),
                account.getHolderName(),
                account.getBalance(),
                account.getStatus(),
                account.getDailyLimit(),
                account.getLastUpdated());
    }
}
