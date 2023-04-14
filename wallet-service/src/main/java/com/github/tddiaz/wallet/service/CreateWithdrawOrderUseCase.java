package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.config.SupportedCurrencyConfiguration.SupportedCurrencies;
import com.github.tddiaz.wallet.config.TransactionLimitsConfiguration.WithdrawalLimit;
import com.github.tddiaz.wallet.controller.dto.WithdrawRequestDto;
import com.github.tddiaz.wallet.controller.dto.WithdrawResponseDto;
import com.github.tddiaz.wallet.exception.AmountNotWithinLimitException;
import com.github.tddiaz.wallet.exception.CurrencyMismatchException;
import com.github.tddiaz.wallet.exception.CurrencyNotSupportedException;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.model.Money;
import com.github.tddiaz.wallet.model.Transaction;
import com.github.tddiaz.wallet.repository.TransactionRepository;
import com.github.tddiaz.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateWithdrawOrderUseCase {
    private final WalletRepository walletRepository;

    private final TransactionRepository transactionRepository;

    private final WithdrawalLimit withdrawalLimit;

    private final SupportedCurrencies supportedCurrencies;

    @Transactional
    public WithdrawResponseDto execute(WithdrawRequestDto request) {
        log.info("processing withdraw order request: {}", request);

        var existingTransaction = transactionRepository.findByReferenceId(request.referenceId());
        if (existingTransaction.isPresent()) {
            Transaction transaction = existingTransaction.get();
            return new WithdrawResponseDto(transaction.getId(), transaction.getStatus().name());
        }

        var withdrawAmount = Money.create(request.amount().currency(), request.amount().value());
        validateWithdrawAmount(withdrawAmount);

        var wallet = walletRepository.findByIdAndCustomerIdWithExclusiveLock(request.walletId(), request.customerId())
            .orElseThrow(() -> new NotFoundException("Wallet not found"));
        if (!wallet.isCurrencyMatched(withdrawAmount.getCurrency())) {
            throw new CurrencyMismatchException(String.format("Customer wallet only supports '%s' as currency", wallet.getCurrency()));
        }

        wallet.withdraw(withdrawAmount);
        walletRepository.save(wallet);

        var withdrawTransaction = Transaction.createWithdrawalRequest(
            wallet.getId(),
            withdrawAmount,
            request.referenceId()
        );
        transactionRepository.save(withdrawTransaction);

        log.debug("withdraw transaction has been successfully saved to DB - {}", withdrawTransaction);

        return new WithdrawResponseDto(withdrawTransaction.getId(), withdrawTransaction.getStatus().name());
    }

    private void validateWithdrawAmount(Money withdrawAmount) {
        if (!supportedCurrencies.isSupported(withdrawAmount.getCurrency())) {
            throw new CurrencyNotSupportedException(String.format("Currency '%s' is not supported", withdrawAmount.getCurrency()));
        }

        if (!withdrawalLimit.isWithinLimit(withdrawAmount.getAmount())) {
            throw new AmountNotWithinLimitException(String.format("Invalid withdraw amount. Max limit is '%s' and Min limit is '%s'",
                withdrawalLimit.getMax().toPlainString(), withdrawalLimit.getMin().toPlainString()));
        }
    }
}
