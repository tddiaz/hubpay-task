package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.config.SupportedCurrencyConfiguration.SupportedCurrencies;
import com.github.tddiaz.wallet.config.TransactionLimitsConfiguration.WithdrawalLimit;
import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.controller.dto.WithdrawRequestDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateWithdrawOrderUseCaseTest {

    private final WalletRepository walletRepository = mock(WalletRepository.class);

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);

    private final WithdrawalLimit withdrawalLimit = new WithdrawalLimit(BigDecimal.valueOf(5000L), new BigDecimal("0.1"));
    private final SupportedCurrencies supportedCurrencies = new SupportedCurrencies(Set.of("GBP", "AED"));

    private final CreateWithdrawOrderUseCase underTest = new CreateWithdrawOrderUseCase(
        walletRepository,
        transactionRepository,
        withdrawalLimit,
        supportedCurrencies
    );

    private final WithdrawRequestDto withdrawRequestDto = new WithdrawRequestDto("REF123", 123L, 123L, new MoneyDto("GBP", BigDecimal.TEN));

    @Test
    void givenWithdrawRequest_whenExecuteAndTransactionWithReferenceIdAlreadyExists_thenReturnExistingTransactionDetails() {
        // given
        var transaction = Transaction.createWithdrawalRequest(123L, Money.create("GBP", BigDecimal.TEN), "REF123");
        when(transactionRepository.findByReferenceId(withdrawRequestDto.referenceId())).thenReturn(Optional.of(transaction));

        // when
        var responseDto = underTest.execute(withdrawRequestDto);

        // then
        assertThat(responseDto.status()).isEqualTo(transaction.getStatus().name());
        assertThat(responseDto.transactionId()).isEqualTo(transaction.getId());

        verify(transactionRepository, never()).save(any());
        verify(walletRepository, never()).findById(any());
    }

    @Test
    void givenWithdrawRequestWithUnsupportedCurrency_whenExecute_thenThrowError() {
        // given
        when(transactionRepository.findByReferenceId(withdrawRequestDto.referenceId())).thenReturn(Optional.empty());

        var withdrawRequestDto = new WithdrawRequestDto("REF123", 123L, 123L, new MoneyDto("USD", BigDecimal.TEN));

        // when then
        assertThrows(CurrencyNotSupportedException.class, () -> underTest.execute(withdrawRequestDto));
    }

    @Test
    void givenWithdrawRequestWithAmountNotWithinLimit_whenExecute_thenThrowError() {
        // given
        when(transactionRepository.findByReferenceId(withdrawRequestDto.referenceId())).thenReturn(Optional.empty());

        var withdrawRequestDto = new WithdrawRequestDto("REF123", 123L, 123L, new MoneyDto("GBP", BigDecimal.valueOf(5001L)));

        // when then
        assertThrows(AmountNotWithinLimitException.class, () -> underTest.execute(withdrawRequestDto));
    }

    @Test
    void givenWithdrawRequestWithInvalidWalletId_whenExecute_thenThrowError() {
        // given
        when(transactionRepository.findByReferenceId(withdrawRequestDto.referenceId())).thenReturn(Optional.empty());
        when(walletRepository.findByIdAndCustomerIdWithExclusiveLock(withdrawRequestDto.walletId(), withdrawRequestDto.customerId())).thenReturn(Optional.empty());

        // when then
        assertThrows(NotFoundException.class, () -> underTest.execute(withdrawRequestDto));
    }

    @Test
    void givenWithdrawRequest_whenExecuteAndWithdrawAmountCurrencyDoesntMatchWithWalletCurrency_thenThrowError() {
        // given
        // deposit amount currency in AED
        var withdrawRequestDto = new WithdrawRequestDto("REF123", 123L, 123L, new MoneyDto("AED", BigDecimal.valueOf(5000L)));
        when(transactionRepository.findByReferenceId(withdrawRequestDto.referenceId())).thenReturn(Optional.empty());

        // wallet currency in GBP
        var wallet = Wallet.initialize(123L, "GBP");
        when(walletRepository.findByIdAndCustomerIdWithExclusiveLock(withdrawRequestDto.walletId(), withdrawRequestDto.customerId())).thenReturn(Optional.of(wallet));

        // when then
        assertThrows(CurrencyMismatchException.class, () -> underTest.execute(withdrawRequestDto));
    }

    @Test
    void givenWithdrawRequest_whenExecuteAndTheresNoExistingTransactionWithReferenceId_thenCreateNewWithdrawTransaction() {

        // given
        when(transactionRepository.findByReferenceId(withdrawRequestDto.referenceId())).thenReturn(Optional.empty());

        var wallet = mock(Wallet.class);
        when(wallet.getId()).thenReturn(1L);
        when(wallet.isCurrencyMatched(any())).thenReturn(true);
        when(walletRepository.findByIdAndCustomerIdWithExclusiveLock(withdrawRequestDto.walletId(), withdrawRequestDto.customerId())).thenReturn(Optional.of(wallet));

        // when
        var responseDto = underTest.execute(withdrawRequestDto);

        // then
        verify(wallet).withdraw(eq(Money.create(withdrawRequestDto.amount().currency(), withdrawRequestDto.amount().value())));
        verify(walletRepository).save(eq(wallet));

        var withdrawTransactionArgumentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(withdrawTransactionArgumentCaptor.capture());

        var transaction = withdrawTransactionArgumentCaptor.getValue();

        assertThat(responseDto.status()).isEqualTo(transaction.getStatus().name());
        assertThat(responseDto.transactionId()).isEqualTo(transaction.getId());
        assertThat(transaction.getWalletId()).isEqualTo(1L);
        assertThat(transaction.getAmount()).isEqualTo(Money.create(withdrawRequestDto.amount().currency(), withdrawRequestDto.amount().value()));
        assertThat(transaction.getReferenceId()).isEqualTo(withdrawRequestDto.referenceId());
    }
}