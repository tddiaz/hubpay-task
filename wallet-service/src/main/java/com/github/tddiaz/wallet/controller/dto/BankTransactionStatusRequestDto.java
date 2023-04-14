package com.github.tddiaz.wallet.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tddiaz.wallet.controller.dto.constraints.ValidEnum;
import jakarta.validation.constraints.NotBlank;

public record BankTransactionStatusRequestDto(
    @NotBlank
    String referenceId,

    @NotBlank
    @ValidEnum(enumClass = BankingStatus.class)
    String status
) {

    @JsonIgnore
    public BankingStatus getStatusEnum() {
        return BankingStatus.valueOf(status);
    }

    public enum BankingStatus {
        SUCCESS,
        FAILED
    }
}
