package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto;
import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto.BankingStatus;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.model.Money;
import com.github.tddiaz.wallet.model.Transaction;
import com.github.tddiaz.wallet.model.TransactionStatus;
import com.github.tddiaz.wallet.model.Wallet;
import com.github.tddiaz.wallet.repository.TransactionRepository;
import com.github.tddiaz.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SettleTransactionUseCaseTest {

    private final WalletRepository walletRepository = mock(WalletRepository.class);

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);

    private final SettleTransactionUseCase underTest = new SettleTransactionUseCase(
        walletRepository,
        transactionRepository
    );

    @Test
    void givenBankTransactionStatusRequestWithUnknownReferenceId_whenExecute_thenThrowError() {
        // given
        var bankTransactionStatusRequestDto = new BankTransactionStatusRequestDto("REF123", BankingStatus.SUCCESS.name());
        when(transactionRepository.findByReferenceIdWithExclusiveLock(bankTransactionStatusRequestDto.referenceId()))
            .thenReturn(Optional.empty());

        // when then
        assertThrows(NotFoundException.class, () -> underTest.execute(bankTransactionStatusRequestDto));
    }

    @Test
    void givenBankTransactionStatusRequest_whenExecuteAndTransactionIsNotPending_thenIgnoreNotificationRequestAndReturnExistingTransaction() {
        // given
        var bankTransactionStatusRequestDto = new BankTransactionStatusRequestDto("REF123", BankingStatus.SUCCESS.name());

        var transaction = mock(Transaction.class);
        when(transaction.isPending()).thenReturn(false);
        when(transactionRepository.findByReferenceIdWithExclusiveLock(bankTransactionStatusRequestDto.referenceId()))
            .thenReturn(Optional.of(transaction));

        // when
        underTest.execute(bankTransactionStatusRequestDto);

        // then
        verifyNoInteractions(walletRepository);
        verify(transactionRepository, never()).save(any());
    }

    @Nested
    class SettleDepositTransactionTest {

        @Test
        void givenBankTransactionStatusRequestForASuccessfulDeposit_whenExecuteAndTransactionIsPending_thenUpdateTransactionStatusToSuccessAndDepositTheFundsToWallet() {
            // given
            var bankTransactionStatusRequestDto = new BankTransactionStatusRequestDto("REF123", BankingStatus.SUCCESS.name());

            // pending deposit transaction
            var depositTransaction = Transaction.createDepositRequest(123L, Money.create("GBP", BigDecimal.TEN), "REF123");
            when(transactionRepository.findByReferenceIdWithExclusiveLock(bankTransactionStatusRequestDto.referenceId()))
                .thenReturn(Optional.of(depositTransaction));

            var wallet = mock(Wallet.class);
            when(walletRepository.findByIdWithExclusiveLock(123L)).thenReturn(Optional.of(wallet));

            // when
            underTest.execute(bankTransactionStatusRequestDto);

            // then
            verify(wallet).deposit(eq(depositTransaction.getAmount()));
            verify(walletRepository).save(eq(wallet));

            verify(transactionRepository).save(eq(depositTransaction));
            assertThat(depositTransaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        }

        @Test
        void givenBankTransactionStatusRequestForAFailedDeposit_whenExecuteAndTransactionIsPending_thenUpdateTransactionStatusToFailed() {
            // given
            var bankTransactionStatusRequestDto = new BankTransactionStatusRequestDto("REF123", BankingStatus.FAILED.name());

            // pending deposit transaction
            var depositTransaction = Transaction.createDepositRequest(123L, Money.create("GBP", BigDecimal.TEN), "REF123");
            when(transactionRepository.findByReferenceIdWithExclusiveLock(bankTransactionStatusRequestDto.referenceId()))
                .thenReturn(Optional.of(depositTransaction));

            var wallet = mock(Wallet.class);
            when(walletRepository.findByIdWithExclusiveLock(123L)).thenReturn(Optional.of(wallet));

            // when
            underTest.execute(bankTransactionStatusRequestDto);

            // then
            verify(wallet, never()).deposit(any());
            verify(walletRepository, never()).save(eq(wallet));

            verify(transactionRepository).save(eq(depositTransaction));
            assertThat(depositTransaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        }
    }

    @Nested
    class SettleWithdrawTransactionTest {

        @Test
        void givenBankTransactionStatusRequestForASuccessfulWithdrawal_whenExecuteAndTransactionIsPending_thenUpdateTransactionStatusToSuccessAndResetHeldFundsOfWallet() {
            // given
            var bankTransactionStatusRequestDto = new BankTransactionStatusRequestDto("REF123", BankingStatus.SUCCESS.name());

            // pending deposit transaction
            var withdrawTransaction = Transaction.createWithdrawalRequest(123L, Money.create("GBP", BigDecimal.TEN), "REF123");
            when(transactionRepository.findByReferenceIdWithExclusiveLock(bankTransactionStatusRequestDto.referenceId()))
                .thenReturn(Optional.of(withdrawTransaction));

            var wallet = mock(Wallet.class);
            when(walletRepository.findByIdWithExclusiveLock(123L)).thenReturn(Optional.of(wallet));

            // when
            underTest.execute(bankTransactionStatusRequestDto);

            // then
            verify(wallet).resetBalanceOnHold();
            verify(wallet, never()).deposit(any());
            verify(walletRepository).save(eq(wallet));

            verify(transactionRepository).save(eq(withdrawTransaction));
            assertThat(withdrawTransaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        }

        @Test
        void givenBankTransactionStatusRequestForAFailedWithdrawal_whenExecuteAndTransactionIsPending_thenUpdateTransactionStatusToFailedAndDepositTheHeldFundsBackToWalletBalance() {
            // given
            var bankTransactionStatusRequestDto = new BankTransactionStatusRequestDto("REF123", BankingStatus.FAILED.name());

            // pending deposit transaction
            var withdrawTransaction = Transaction.createWithdrawalRequest(123L, Money.create("GBP", BigDecimal.TEN), "REF123");
            when(transactionRepository.findByReferenceIdWithExclusiveLock(bankTransactionStatusRequestDto.referenceId()))
                .thenReturn(Optional.of(withdrawTransaction));

            var wallet = mock(Wallet.class);
            when(walletRepository.findByIdWithExclusiveLock(123L)).thenReturn(Optional.of(wallet));

            // when
            underTest.execute(bankTransactionStatusRequestDto);

            // then
            verify(wallet).deposit(eq(withdrawTransaction.getAmount()));
            verify(wallet).resetBalanceOnHold();
            verify(walletRepository).save(eq(wallet));

            verify(transactionRepository).save(eq(withdrawTransaction));
            assertThat(withdrawTransaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        }
    }

}