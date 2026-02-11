package com.mts.application.dto;

import com.mts.domain.enums.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        String id,
        String idempotencyKey,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String failureReason,
        LocalDateTime createdOn) {
}
