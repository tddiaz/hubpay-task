package com.github.tddiaz.wallet.model;

import com.github.f4b6a3.tsid.TsidCreator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    private Long walletId = TsidCreator.getTsid().toLong();
    private String referenceId = "REF123";
    private Money transactionAmount = Money.create("GBP", BigDecimal.TEN);

    @Test
    void givenWalletIdAndReferenceIdAndAmount_whenCreateDepositRequest_thenReturnNewInstance() {

        // when
        var transaction = Transaction.createDepositRequest(walletId, transactionAmount, referenceId);

        // then
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getReferenceId()).isEqualTo(referenceId);
        assertThat(transaction.getWalletId()).isEqualTo(walletId);
        assertThat(transaction.getAmount()).isEqualTo(transactionAmount);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getType()).isEqualTo(TransactionType.BANK_TRANSFER);
        assertThat(transaction.getEntry()).isEqualTo(TransactionEntry.DEPOSIT);
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @Test
    void givenWalletIdAndReferenceIdAndAmount_whenCreateWithdrawalRequest_thenReturnNewInstance() {
        // when
        var transaction = Transaction.createWithdrawalRequest(walletId, transactionAmount, referenceId);

        // then
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getReferenceId()).isEqualTo(referenceId);
        assertThat(transaction.getWalletId()).isEqualTo(walletId);
        assertThat(transaction.getAmount()).isEqualTo(transactionAmount);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getType()).isEqualTo(TransactionType.BANK_TRANSFER);
        assertThat(transaction.getEntry()).isEqualTo(TransactionEntry.WITHDRAW);
        assertThat(transaction.getCreatedAt()).isNotNull();
    }

    @Test
    void whenSuccess_thenUpdateStatusAsSuccess() {
        // given
        var transaction = Transaction.createDepositRequest(walletId, transactionAmount, referenceId);

        // when
        transaction.success();

        // then
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    void whenFailed_thenUpdateStatusAFailed() {
        // given
        var transaction = Transaction.createDepositRequest(walletId, transactionAmount, referenceId);

        // when
        transaction.failed();

        // then
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

}