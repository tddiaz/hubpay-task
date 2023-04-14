package com.github.tddiaz.wallet.model;

import com.github.tddiaz.wallet.exception.CurrencyMismatchException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Setter(AccessLevel.NONE)
public class Money {
    private String currency;

    private BigDecimal amount;

    public static Money create(String currency, BigDecimal amount) {
        return new Money(currency, amount);
    }

    public Money add(Money money) {
        if (!Objects.equals(this.currency, money.getCurrency())) {
            throw new CurrencyMismatchException("Cannot add money with different currency");
        }

        return Money.create(this.currency, this.amount.add(money.amount));
    }

    public Money subtract(Money money) {
        if (!Objects.equals(this.currency, money.getCurrency())) {
            throw new CurrencyMismatchException("Cannot subtract money with different currency");
        }

        return Money.create(this.currency, this.amount.subtract(money.amount));
    }

    @Transient
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    @Transient
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    @Transient
    public boolean isGreaterThan(Money money) {
        if (!Objects.equals(this.currency, money.getCurrency())) {
            throw new CurrencyMismatchException("Cannot compare money with different currency");
        }

        return this.amount.compareTo(money.getAmount()) > 0;
    }

    @Transient
    public String toFormattedString() {
        return String.format("%s %s", this.currency, this.amount.toPlainString());
    }

}
