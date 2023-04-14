package com.github.tddiaz.wallet.controller.dto;

public record DepositResponseDto(
    Long transactionId,
    String status
) {
}
