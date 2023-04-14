package com.github.tddiaz.wallet.controller.dto;

public record WithdrawResponseDto(
    Long transactionId,
    String status
) {
}
