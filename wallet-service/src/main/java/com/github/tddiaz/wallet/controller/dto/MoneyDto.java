package com.github.tddiaz.wallet.controller.dto;

import com.github.tddiaz.wallet.controller.dto.constraints.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MoneyDto(
    @NotBlank
    @ValidCurrency
    String currency,

    @NotNull
    BigDecimal value
) {
}
