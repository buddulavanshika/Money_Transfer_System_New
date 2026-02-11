package com.mts.application.controller;

import com.mts.application.dto.TransferApprovalRequest;
import com.mts.application.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/transfers/approvals")
@PreAuthorize("hasRole('ADMIN')")
public class TransferApprovalController {

    private final AdminService adminService;

    public TransferApprovalController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> processApproval(@PathVariable Long id, @RequestBody TransferApprovalRequest request) {
        if (request.approved()) {
            adminService.approveTransfer(id);
        } else {
            adminService.rejectTransfer(id, request.rejectionReason());
        }
        return ResponseEntity.ok().build();
    }
}
