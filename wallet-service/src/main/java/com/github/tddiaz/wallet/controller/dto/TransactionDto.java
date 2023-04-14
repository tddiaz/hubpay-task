package com.github.tddiaz.wallet.controller.dto;

import java.time.LocalDateTime;

public record TransactionDto(
    Long id,
    String referenceId,
    String type,
    String entry,
    LocalDateTime createdAt,

    MoneyDto amount
) {
}
