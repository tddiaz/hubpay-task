package com.github.tddiaz.wallet.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepositRequestDto(
    @NotBlank
    String referenceId,

    @NotNull
    Long walletId,

    @NotNull
    Long customerId,

    @NotNull
    @Valid
    MoneyDto amount
) {
}
