package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.controller.dto.GetTransactionsRequestDto;
import com.github.tddiaz.wallet.controller.dto.GetTransactionsResponseDto;
import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.controller.dto.TransactionDto;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.repository.TransactionRepository;
import com.github.tddiaz.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTransactionsUseCase {

    private final WalletRepository walletRepository;

    private final TransactionRepository transactionRepository;

    public GetTransactionsResponseDto execute(GetTransactionsRequestDto request) {
        var wallet = walletRepository.findByIdAndCustomerId(request.walletId(), request.customerId())
            .orElseThrow(() -> new NotFoundException("Wallet not found"));

        var queryResult = transactionRepository.findAllByWalletId(
            wallet.getId(),
            PageRequest.of(request.pageNumber(), request.size(), Sort.by("createdAt").descending())
        );

        var content = queryResult.getContent().stream().map(transaction ->
            new TransactionDto(
                transaction.getId(),
                transaction.getReferenceId(),
                transaction.getType().name(),
                transaction.getEntry().name(),
                transaction.getCreatedAt(),
                new MoneyDto(transaction.getAmount().getCurrency(), transaction.getAmount().getAmount())
            )).toList();

        return new GetTransactionsResponseDto(
            queryResult.getTotalElements(),
            queryResult.getTotalPages(),
            queryResult.getNumber(),
            queryResult.getContent().size(),
            content
        );
    }
}
