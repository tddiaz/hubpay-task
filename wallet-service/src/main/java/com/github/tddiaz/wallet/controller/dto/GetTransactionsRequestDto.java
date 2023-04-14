package com.github.tddiaz.wallet.controller.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record GetTransactionsRequestDto(
    @NotNull
    Long walletId,

    @NotNull
    Long customerId,

    Integer pageNumber,

    Integer size
) {

    public int getPageNumber() {
        return Objects.nonNull(pageNumber) ? pageNumber : 0;
    }

    public int getSize() {
        return Objects.nonNull(size) ? size : 10;
    }
}
