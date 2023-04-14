package com.github.tddiaz.wallet.model;

import com.github.tddiaz.wallet.exception.CurrencyMismatchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void givenCurrencyAndAmount_whenCreate_thenReturnMoneyInstance() {
        // GIVEN
        var currency = "GBP";
        var amount = BigDecimal.TEN;

        // WHEN
        var money = Money.create(currency, amount);

        // THEN
        assertThat(money.getCurrency()).isEqualTo(currency);
        assertThat(money.getAmount()).isEqualTo(amount);
    }

    @Nested
    class AddTest {

        @Test
        void givenMoney_whenAddAndCurrencyDoesntMatch_thenThrowError() {
            var gbp = Money.create("GBP", BigDecimal.TEN);
            var usd = Money.create("USD", BigDecimal.TEN);

            Assertions.assertThrows(CurrencyMismatchException.class, () -> gbp.add(usd));
        }

        @Test
        void givenMoney_whenAddAndCurrencyMatch_thenReturnNewValue() {
            var result = Money.create("USD", BigDecimal.TEN).add(Money.create("USD", BigDecimal.TEN));

            assertThat(result).isEqualTo(Money.create("USD", BigDecimal.valueOf(20L)));
        }
    }

    @Nested
    class SubtractTest {

        @Test
        void givenMoney_whenSubtractAndCurrencyDoesntMatch_thenThrowError() {
            var gbp = Money.create("GBP", BigDecimal.TEN);
            var usd = Money.create("USD", BigDecimal.TEN);

            Assertions.assertThrows(CurrencyMismatchException.class, () -> gbp.subtract(usd));
        }

        @Test
        void givenMoney_whenSubtractAndCurrencyMatch_thenReturnNewValue() {
            var result = Money.create("USD", BigDecimal.TEN).subtract(Money.create("USD", BigDecimal.TEN));

            assertThat(result).isEqualTo(Money.create("USD", BigDecimal.ZERO));
        }
    }

    @Nested
    class IsNegativeTest {
        @Test
        void givenMoneyThatHasNegativeAmount_whenIsNegative_thenReturnTrue() {
            Money money = Money.create("USD", BigDecimal.valueOf(-1L));

            assertThat(money.isNegative()).isTrue();
        }

        @Test
        void givenMoneyThatHasZeroAmount_whenIsNegative_thenReturnFalse() {
            Money money = Money.create("USD", BigDecimal.ZERO);

            assertThat(money.isNegative()).isFalse();
        }

        @Test
        void givenMoneyThatHasPositiveAmount_whenIsNegative_thenReturnFalse() {
            Money money = Money.create("USD", BigDecimal.ONE);

            assertThat(money.isNegative()).isFalse();
        }
    }

    @Nested
    class IsZeroTest {
        @Test
        void givenMoneyThatHasNegativeAmount_whenIsZero_thenReturnFalse() {
            Money money = Money.create("USD", BigDecimal.valueOf(-1L));

            assertThat(money.isZero()).isFalse();
        }

        @Test
        void givenMoneyThatHasZeroAmount_whenIsZero_thenReturnTrue() {
            Money money = Money.create("USD", BigDecimal.ZERO);

            assertThat(money.isZero()).isTrue();
        }

        @Test
        void givenMoneyThatHasPositiveAmount_whenIsZero_thenReturnFalse() {
            Money money = Money.create("USD", BigDecimal.ONE);

            assertThat(money.isZero()).isFalse();
        }
    }

    @Test
    void givenMoney_whenToFormattedString_thenReturnStringValue() {
        Money money = Money.create("USD", new BigDecimal("1000.01"));

        assertThat(money.toFormattedString()).isEqualTo("USD 1000.01");
    }
}