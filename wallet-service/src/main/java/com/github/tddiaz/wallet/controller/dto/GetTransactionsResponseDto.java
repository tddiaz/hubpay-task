package com.github.tddiaz.wallet.controller.dto;

import java.util.List;

public record GetTransactionsResponseDto(
    long totalCount,

    int totalPages,

    int pageNumber,

    int numberOfElements,

    List<TransactionDto> data
) {
}
