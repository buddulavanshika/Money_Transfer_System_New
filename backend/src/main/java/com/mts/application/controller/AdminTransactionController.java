package com.mts.application.controller;

import com.mts.application.dto.TransactionFilter;
import com.mts.application.dto.TransactionResponse;
import com.mts.application.service.AdminService;
import com.mts.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransactionController {

    private final AdminService adminService;

    public AdminTransactionController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> searchTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            Pageable pageable) {

        TransactionFilter filter = new TransactionFilter(
                accountId, status, minAmount, maxAmount, fromDate, toDate);
        return ResponseEntity.ok(adminService.searchTransactions(filter, pageable));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<Void> reverseTransaction(@PathVariable String id,
            @RequestBody(required = false) String reason) {
        adminService.reverseTransaction(id, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/limits/global")
    public ResponseEntity<Void> setGlobalLimit(@RequestBody BigDecimal limit) {
        adminService.setGlobalTransferLimit(limit);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/limits/global")
    public ResponseEntity<BigDecimal> getGlobalLimit() {
        return ResponseEntity.ok(adminService.getGlobalTransferLimit());
    }
}
