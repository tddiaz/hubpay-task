package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.controller.dto.GetWalletDetailsResponseDto;
import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWalletDetailsUseCase {

    private final WalletRepository walletRepository;

    public GetWalletDetailsResponseDto execute(Long walletId, Long customerId) {
        var wallet = walletRepository.findByIdAndCustomerId(walletId, customerId)
            .orElseThrow(() -> new NotFoundException("Wallet not found"));

        return new GetWalletDetailsResponseDto(
            wallet.getId(),
            wallet.getCustomerId(),
            new MoneyDto(wallet.getBalance().getCurrency(), wallet.getBalance().getAmount()),
            new MoneyDto(wallet.getBalanceOnHold().getCurrency(), wallet.getBalanceOnHold().getAmount())
        );
    }
}
