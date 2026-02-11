package com.mts.application.dto;

import java.math.BigDecimal;

public record AccountUpdateRequest(
        String holderName,
        BigDecimal dailyLimit) {
}
