package com.github.tddiaz.wallet.model;

import com.github.f4b6a3.tsid.TsidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Entity
@Accessors(chain = true)
public class Transaction {
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String referenceId;

    @Column(nullable = false)
    private Long walletId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "currency", column = @Column(name = "transaction_currency", nullable = false)),
        @AttributeOverride(name = "amount", column = @Column(name = "transaction_amount", nullable = false))
    })
    private Money amount;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, name = "transaction_type")
    private TransactionType type;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private TransactionEntry entry;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Transaction createDepositRequest(Long walletId, Money transactionAmount, String referenceId) {
        return new Transaction()
            .setId(TsidCreator.getTsid().toLong())
            .setReferenceId(referenceId)
            .setWalletId(walletId)
            .setAmount(transactionAmount)
            .setStatus(TransactionStatus.PENDING)
            .setType(TransactionType.BANK_TRANSFER)
            .setEntry(TransactionEntry.DEPOSIT)
            .setCreatedAt(LocalDateTime.now());
    }

    public static Transaction createWithdrawalRequest(Long walletId, Money transactionAmount, String referenceId) {
        return new Transaction()
            .setId(TsidCreator.getTsid().toLong())
            .setReferenceId(referenceId)
            .setWalletId(walletId)
            .setAmount(transactionAmount)
            .setStatus(TransactionStatus.PENDING)
            .setType(TransactionType.BANK_TRANSFER)
            .setEntry(TransactionEntry.WITHDRAW)
            .setCreatedAt(LocalDateTime.now());
    }

    public void success() {
        this.status = TransactionStatus.SUCCESS;
    }

    public void failed() {
        this.status = TransactionStatus.FAILED;
    }

    @Transient
    public Boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }
}
