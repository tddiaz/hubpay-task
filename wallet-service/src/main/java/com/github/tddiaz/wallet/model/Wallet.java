package com.github.tddiaz.wallet.model;

import com.github.f4b6a3.tsid.TsidCreator;
import com.github.tddiaz.wallet.exception.InsufficientFundsException;
import com.github.tddiaz.wallet.exception.InvalidAmountException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Accessors(chain = true)
public class Wallet {
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private Long customerId;

    @AttributeOverrides({
        @AttributeOverride(name = "currency", column = @Column(name = "balance_currency", nullable = false)),
        @AttributeOverride(name = "amount", column = @Column(name = "balance_amount", nullable = false))
    })
    @Embedded
    private Money balance;

    @AttributeOverrides({
        @AttributeOverride(name = "currency", column = @Column(name = "balance_held_currency", nullable = false)),
        @AttributeOverride(name = "amount", column = @Column(name = "balance_held_amount", nullable = false))
    })
    @Embedded
    private Money balanceOnHold;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Wallet initialize(Long customerId, String currency) {
        return new Wallet()
            .setId(TsidCreator.getTsid().toLong())
            .setCustomerId(customerId)
            .setBalance(Money.create(currency, BigDecimal.ZERO))
            .setBalanceOnHold(Money.create(currency, BigDecimal.ZERO))
            .setCreatedAt(LocalDateTime.now());
    }

    public void deposit(Money fundsToDeposit) {
        if (fundsToDeposit.isZero() || fundsToDeposit.isNegative()) {
            throw new InvalidAmountException("Deposit funds cannot be zero or negative. Amount is " + fundsToDeposit.toFormattedString());
        }

        this.balance = this.balance.add(fundsToDeposit);
    }

    public void withdraw(Money fundsToWithdraw) {
        if (fundsToWithdraw.isZero() || fundsToWithdraw.isNegative()) {
            throw new InvalidAmountException("Withdraw funds cannot be zero or negative. Amount is " + fundsToWithdraw.toFormattedString());
        }

        if (fundsToWithdraw.isGreaterThan(this.balance)) {
            throw new InsufficientFundsException("Not enough funds to withdraw. Remaining balance is only " + this.balance.toFormattedString());
        }

        this.balance = this.balance.subtract(fundsToWithdraw);
        this.balanceOnHold = this.balanceOnHold.add(fundsToWithdraw);
    }

    public void resetBalanceOnHold() {
        this.balanceOnHold = Money.create(this.balance.getCurrency(), BigDecimal.ZERO);
    }

    @Transient
    public boolean isCurrencyMatched(String currency) {
        return Objects.equals(this.getCurrency(), currency);
    }

    @Transient
    public String getCurrency() {
        return this.balance.getCurrency();
    }
}
