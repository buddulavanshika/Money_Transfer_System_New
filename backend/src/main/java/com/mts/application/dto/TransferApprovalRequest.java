package com.mts.application.dto;

import jakarta.validation.constraints.NotNull;

public record TransferApprovalRequest(
        @NotNull boolean approved,
        String rejectionReason // Optional
) {
}
