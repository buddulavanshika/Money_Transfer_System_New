package com.mts.application.dto;

import com.mts.domain.enums.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionFilter(
        String accountId,
        TransactionStatus status,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        LocalDateTime fromDate,
        LocalDateTime toDate) {
}
