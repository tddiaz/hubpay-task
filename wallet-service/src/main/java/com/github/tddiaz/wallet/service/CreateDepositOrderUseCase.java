package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.config.SupportedCurrencyConfiguration.SupportedCurrencies;
import com.github.tddiaz.wallet.config.TransactionLimitsConfiguration.DepositLimit;
import com.github.tddiaz.wallet.controller.dto.DepositRequestDto;
import com.github.tddiaz.wallet.controller.dto.DepositResponseDto;
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
public class CreateDepositOrderUseCase {
    private final WalletRepository walletRepository;

    private final TransactionRepository transactionRepository;

    private final DepositLimit depositLimit;

    private final SupportedCurrencies supportedCurrencies;

    @Transactional
    public DepositResponseDto execute(DepositRequestDto request) {
        log.info("processing deposit order request: {}", request);

        var existingTransaction = transactionRepository.findByReferenceId(request.referenceId());
        if (existingTransaction.isPresent()) {
            Transaction transaction = existingTransaction.get();
            return new DepositResponseDto(transaction.getId(), transaction.getStatus().name());
        }

        var depositAmount = Money.create(request.amount().currency(), request.amount().value());
        validateDepositAmount(depositAmount);

        var wallet = walletRepository.findByIdAndCustomerId(request.walletId(), request.customerId())
            .orElseThrow(() -> new NotFoundException("Wallet not found"));
        if (!wallet.isCurrencyMatched(depositAmount.getCurrency())) {
            throw new CurrencyMismatchException(String.format("Customer wallet only supports '%s' as currency", wallet.getCurrency()));
        }

        var depositTransaction = Transaction.createDepositRequest(
            wallet.getId(),
            depositAmount,
            request.referenceId()
        );
        transactionRepository.save(depositTransaction);

        log.info("deposit transaction has been successfully saved to DB - {}", depositTransaction);

        return new DepositResponseDto(depositTransaction.getId(), depositTransaction.getStatus().name());
    }

    private void validateDepositAmount(Money depositAmount) {
        if (!supportedCurrencies.isSupported(depositAmount.getCurrency())) {
            throw new CurrencyNotSupportedException(String.format("Currency '%s' is not supported", depositAmount.getCurrency()));
        }

        if (!depositLimit.isWithinLimit(depositAmount.getAmount())) {
            throw new AmountNotWithinLimitException(String.format("Invalid deposit amount. Max limit is '%s' and Min limit is '%s'",
                depositLimit.getMax().toPlainString(), depositLimit.getMin().toPlainString()));
        }
    }
}

