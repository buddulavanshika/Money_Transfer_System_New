package com.mts.application.controller;

import com.mts.application.dto.AccountCreateRequest;
import com.mts.application.dto.AccountResponse;
import com.mts.application.dto.AccountUpdateRequest;
import com.mts.application.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Account Management", description = "Admin endpoints for managing user accounts")
public class AdminAccountController {

    private final AdminService adminService;

    public AdminAccountController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Create a new account", description = "Creates a new account with the specified holder name and opening balance. Only accessible by admin users.", responses = {
            @ApiResponse(responseCode = "200", description = "Account created successfully", content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - admin role required", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest req) {
        return ResponseEntity.ok(adminService.createAccount(req));
    }

    @Operation(summary = "Update an existing account", description = "Updates account holder name and/or daily limit for the specified account ID. Only accessible by admin users.", responses = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully", content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Access denied - admin role required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable String id,
            @Valid @RequestBody AccountUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateAccount(id, req));
    }

    @Operation(summary = "Delete an account", description = "Permanently deletes the specified account. This action cannot be undone. Only accessible by admin users.", responses = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - admin role required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        adminService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
