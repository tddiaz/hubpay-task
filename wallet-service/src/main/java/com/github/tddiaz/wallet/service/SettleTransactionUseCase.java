package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.model.Transaction;
import com.github.tddiaz.wallet.repository.TransactionRepository;
import com.github.tddiaz.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto.BankingStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettleTransactionUseCase {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void execute(BankTransactionStatusRequestDto request) {
        log.info("processing bank transaction status notification request: {}", request);

        var transaction = transactionRepository.findByReferenceIdWithExclusiveLock(request.referenceId())
            .orElseThrow(() -> new NotFoundException(String.format("Transaction with reference %s not found", request.referenceId())));

        if (!transaction.isPending()) {
            // skip request since transaction has already been updated from pending state
            return;
        }

        switch (transaction.getEntry()) {
            case DEPOSIT -> settleDepositTransaction(transaction, request.getStatusEnum());
            case WITHDRAW -> settleWithdrawTransaction(transaction, request.getStatusEnum());
        }
    }

    private void settleDepositTransaction(Transaction depositTransaction, BankingStatus bankingStatus) {
        switch (bankingStatus) {
            case SUCCESS -> {
                var wallet = walletRepository.findByIdWithExclusiveLock(depositTransaction.getWalletId())
                    .orElseThrow(() -> new NotFoundException("wallet not found"));

                wallet.deposit(depositTransaction.getAmount());
                walletRepository.save(wallet);

                depositTransaction.success();
                transactionRepository.save(depositTransaction);
            }
            case FAILED -> {
                depositTransaction.failed();
                transactionRepository.save(depositTransaction);
            }
        }
    }

    private void settleWithdrawTransaction(Transaction withdrawTransaction, BankingStatus bankingStatus) {
        var wallet = walletRepository.findByIdWithExclusiveLock(withdrawTransaction.getWalletId())
            .orElseThrow(() -> new NotFoundException("wallet not found"));

        switch (bankingStatus) {
            case SUCCESS -> {
                wallet.resetBalanceOnHold();
                walletRepository.save(wallet);

                withdrawTransaction.success();
                transactionRepository.save(withdrawTransaction);
            }
            case FAILED -> {
                wallet.deposit(withdrawTransaction.getAmount());
                wallet.resetBalanceOnHold();
                walletRepository.save(wallet);

                withdrawTransaction.failed();
                transactionRepository.save(withdrawTransaction);
            }
        }
    }
}
