package com.github.tddiaz.wallet.model;

import com.github.f4b6a3.tsid.TsidCreator;
import com.github.tddiaz.wallet.exception.InsufficientFundsException;
import com.github.tddiaz.wallet.exception.InvalidAmountException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WalletTest {

    private static final Long CUSTOMER_ID = TsidCreator.getTsid().toLong();
    private static final String CURRENCY = "GBP";

    @Test
    void givenCurrencyAndCustomerId_whenInitialize_thenReturnInstance() {
        // given
        // when
        var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);

        assertThat(wallet.getId()).isNotNull();
        assertThat(wallet.getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(wallet.getBalance()).isEqualTo(Money.create("GBP", BigDecimal.ZERO));
        assertThat(wallet.getBalanceOnHold()).isEqualTo(Money.create("GBP", BigDecimal.ZERO));
        assertThat(wallet.getCreatedAt()).isNotNull();
    }

    @Nested
    class DepositTest {

        @Test
        void givenZeroFundAmount_whenDeposit_thenThrowError() {
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);
            assertThrows(InvalidAmountException.class, () -> wallet.deposit(Money.create(CURRENCY, BigDecimal.ZERO)));
        }

        @Test
        void givenNegativeFundAmount_whenDeposit_thenThrowError() {
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);
            assertThrows(InvalidAmountException.class, () -> wallet.deposit(Money.create(CURRENCY, BigDecimal.valueOf(-1L))));
        }

        @Test
        void givenValidFundAmount_whenDeposit_thenUpdateWalletBalance() {
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);

            wallet.deposit(Money.create(CURRENCY, BigDecimal.TEN));
            assertThat(wallet.getBalance()).isEqualTo(Money.create(CURRENCY, BigDecimal.TEN));

            wallet.deposit(Money.create(CURRENCY, BigDecimal.ONE));
            assertThat(wallet.getBalance()).isEqualTo(Money.create(CURRENCY, BigDecimal.valueOf(11L)));
        }
    }

    @Nested
    class WithdrawTest {
        @Test
        void givenZeroFundAmount_whenWithdraw_thenThrowError() {
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);
            assertThrows(InvalidAmountException.class, () -> wallet.withdraw(Money.create(CURRENCY, BigDecimal.ZERO)));
        }

        @Test
        void givenNegativeFundAmount_whenWithdraw_thenThrowError() {
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);
            assertThrows(InvalidAmountException.class, () -> wallet.withdraw(Money.create(CURRENCY, BigDecimal.valueOf(-1L))));
        }

        @Test
        void givenFundsGreaterThanCurrentBalance_whenWithdraw_thenThrowError() {
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);
            wallet.deposit(Money.create(CURRENCY, BigDecimal.TEN));

            assertThrows(InsufficientFundsException.class, () -> wallet.withdraw(Money.create(CURRENCY, BigDecimal.valueOf(11L))));
        }

        @Test
        void givenFundsEqualsToCurrentBalance_whenWithdraw_thenAcceptWithdrawalAndUpdateWalletBalanceAndOnHoldBalance() {
            // given
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);
            wallet.deposit(Money.create(CURRENCY, BigDecimal.TEN));

            // when
            wallet.withdraw(Money.create(CURRENCY, BigDecimal.TEN));

            // then
            assertThat(wallet.getBalance()).isEqualTo(Money.create(CURRENCY, BigDecimal.ZERO));
            assertThat(wallet.getBalanceOnHold()).isEqualTo(Money.create(CURRENCY, BigDecimal.TEN));
        }

        @Test
        void givenFundsLessThaCurrentBalance_whenWithdraw_thenAcceptWithdrawalAndUpdateWalletBalanceAndOnHoldBalance() {
            // given
            var wallet = Wallet.initialize(CUSTOMER_ID, CURRENCY);
            wallet.deposit(Money.create(CURRENCY, BigDecimal.TEN));

            // when
            wallet.withdraw(Money.create(CURRENCY, BigDecimal.ONE));

            // then
            assertThat(wallet.getBalance()).isEqualTo(Money.create(CURRENCY, BigDecimal.valueOf(9L)));
            assertThat(wallet.getBalanceOnHold()).isEqualTo(Money.create(CURRENCY, BigDecimal.ONE));
        }
    }
}