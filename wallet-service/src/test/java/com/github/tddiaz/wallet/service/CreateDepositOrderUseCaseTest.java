package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.config.SupportedCurrencyConfiguration.SupportedCurrencies;
import com.github.tddiaz.wallet.config.TransactionLimitsConfiguration.DepositLimit;
import com.github.tddiaz.wallet.controller.dto.DepositRequestDto;
import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.exception.AmountNotWithinLimitException;
import com.github.tddiaz.wallet.exception.CurrencyMismatchException;
import com.github.tddiaz.wallet.exception.CurrencyNotSupportedException;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.model.Money;
import com.github.tddiaz.wallet.model.Transaction;
import com.github.tddiaz.wallet.model.Wallet;
import com.github.tddiaz.wallet.repository.TransactionRepository;
import com.github.tddiaz.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CreateDepositOrderUseCaseTest {

    private final WalletRepository walletRepository = mock(WalletRepository.class);
    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final DepositLimit depositLimit = new DepositLimit(BigDecimal.valueOf(10_000L), BigDecimal.valueOf(10L));
    private final SupportedCurrencies supportedCurrencies = new SupportedCurrencies(Set.of("GBP", "AED"));

    private final CreateDepositOrderUseCase underTest = new CreateDepositOrderUseCase(
        walletRepository,
        transactionRepository,
        depositLimit,
        supportedCurrencies
    );

    private final DepositRequestDto depositRequestDto = new DepositRequestDto(
        "REF123", 123L, 123L, new MoneyDto("GBP", BigDecimal.TEN));

    @Test
    void givenDepositRequest_whenExecuteAndTransactionWithReferenceIdAlreadyExists_thenReturnExistingTransactionDetails() {
        // given
        var transaction = Transaction.createDepositRequest(123L, Money.create("GBP", BigDecimal.TEN), "REF123");
        when(transactionRepository.findByReferenceId(depositRequestDto.referenceId())).thenReturn(Optional.of(transaction));

        // when
        var responseDto = underTest.execute(depositRequestDto);

        // then
        assertThat(responseDto.status()).isEqualTo(transaction.getStatus().name());
        assertThat(responseDto.transactionId()).isEqualTo(transaction.getId());

        verify(transactionRepository, never()).save(any());
        verify(walletRepository, never()).findById(any());
    }

    @Test
    void givenDepositRequestWithUnsupportedCurrency_whenExecute_thenThrowError() {
        // given
        when(transactionRepository.findByReferenceId(depositRequestDto.referenceId())).thenReturn(Optional.empty());

        var depositRequestDto = new DepositRequestDto("REF123", 123L, 123L, new MoneyDto("USD", BigDecimal.TEN));

        // when then
        assertThrows(CurrencyNotSupportedException.class, () -> underTest.execute(depositRequestDto));
    }

    @Test
    void givenDepositRequestWithAmountNotWithinLimit_whenExecute_thenThrowError() {
        // given
        when(transactionRepository.findByReferenceId(depositRequestDto.referenceId())).thenReturn(Optional.empty());

        var depositRequestDto = new DepositRequestDto("REF123", 123L, 123L, new MoneyDto("GBP", BigDecimal.valueOf(10_001)));

        // when then
        assertThrows(AmountNotWithinLimitException.class, () -> underTest.execute(depositRequestDto));
    }

    @Test
    void givenDepositRequestWithInvalidWalletId_whenExecute_thenThrowError() {
        // given
        when(transactionRepository.findByReferenceId(depositRequestDto.referenceId())).thenReturn(Optional.empty());
        when(walletRepository.findByIdAndCustomerId(depositRequestDto.walletId(), depositRequestDto.customerId())).thenReturn(Optional.empty());

        // when then
        assertThrows(NotFoundException.class, () -> underTest.execute(depositRequestDto));
    }

    @Test
    void givenDepositRequest_whenExecuteAndDepositAmountCurrencyDoesntMatchWithWalletCurrency_thenThrowError() {
        // given
        // deposit amount currency in AED
        var depositRequestDto = new DepositRequestDto("REF123", 123L, 123L, new MoneyDto("AED", BigDecimal.valueOf(10_000)));
        when(transactionRepository.findByReferenceId(depositRequestDto.referenceId())).thenReturn(Optional.empty());

        // wallet currency in GBP
        var wallet = Wallet.initialize(123L, "GBP");
        when(walletRepository.findByIdAndCustomerId(depositRequestDto.walletId(), depositRequestDto.customerId())).thenReturn(Optional.of(wallet));

        // when then
        assertThrows(CurrencyMismatchException.class, () -> underTest.execute(depositRequestDto));
    }

    @Test
    void givenDepositRequest_whenExecuteAndTheresNoExistingTransactionWithReferenceId_thenCreateNewDepositTransaction() {

        // given
        when(transactionRepository.findByReferenceId(depositRequestDto.referenceId())).thenReturn(Optional.empty());

        var wallet = Wallet.initialize(123L, "GBP");
        when(walletRepository.findByIdAndCustomerId(depositRequestDto.walletId(), depositRequestDto.customerId())).thenReturn(Optional.of(wallet));

        // when
        var responseDto = underTest.execute(depositRequestDto);

        // then
        var depositTransactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(depositTransactionArgumentCaptor.capture());

        var transaction = depositTransactionArgumentCaptor.getValue();

        assertThat(responseDto.status()).isEqualTo(transaction.getStatus().name());
        assertThat(responseDto.transactionId()).isEqualTo(transaction.getId());
        assertThat(transaction.getWalletId()).isEqualTo(wallet.getId());
        assertThat(transaction.getAmount()).isEqualTo(Money.create(depositRequestDto.amount().currency(), depositRequestDto.amount().value()));
        assertThat(transaction.getReferenceId()).isEqualTo(depositRequestDto.referenceId());
    }

}