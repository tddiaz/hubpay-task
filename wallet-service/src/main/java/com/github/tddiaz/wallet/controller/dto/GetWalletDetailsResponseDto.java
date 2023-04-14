package com.github.tddiaz.wallet.controller.dto;

public record GetWalletDetailsResponseDto(
    Long walletId,

    Long customerId,

    MoneyDto totalBalance,

    MoneyDto amountOnHold
) {
}
