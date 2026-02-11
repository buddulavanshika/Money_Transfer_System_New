package com.mts.application.dto;

import com.mts.domain.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        String id,
        String holderName,
        BigDecimal balance,
        AccountStatus status,
        BigDecimal dailyLimit,
        Instant lastUpdated) {
}
