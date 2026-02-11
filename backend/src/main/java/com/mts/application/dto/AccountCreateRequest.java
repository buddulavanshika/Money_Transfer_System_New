package com.mts.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AccountCreateRequest(
        @NotBlank String holderName,
        @NotNull BigDecimal openingBalance) {
}
