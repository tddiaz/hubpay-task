package com.github.tddiaz.wallet.repository;

import com.github.f4b6a3.tsid.TsidCreator;
import com.github.tddiaz.wallet.BaseRepositoryIT;
import com.github.tddiaz.wallet.model.Money;
import com.github.tddiaz.wallet.model.Transaction;
import com.github.tddiaz.wallet.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private TransactionRepository underTest;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void findAllByWalletIdTest() {
        var wallet = Wallet.initialize(TsidCreator.getTsid().toLong(), "GBP");
        transactionTemplate.execute(action ->
            walletRepository.save(wallet)
        );

        var amount = Money.create("GBP", BigDecimal.TEN);

        var transaction1 = Transaction.createDepositRequest(wallet.getId(), amount, UUID.randomUUID().toString());
        var transaction2 = Transaction.createDepositRequest(wallet.getId(), amount, UUID.randomUUID().toString());
        var transaction3 = Transaction.createDepositRequest(wallet.getId(), amount, UUID.randomUUID().toString());
        var transaction4 = Transaction.createDepositRequest(wallet.getId(), amount, UUID.randomUUID().toString());
        var transaction5 = Transaction.createDepositRequest(wallet.getId(), amount, UUID.randomUUID().toString());

        transactionTemplate.execute(action -> underTest.saveAll(List.of(transaction1, transaction2, transaction3, transaction4, transaction5)));

        {
            var pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());
            var transactions = underTest.findAllByWalletId(wallet.getId(), pageable);

            assertThat(transactions.getTotalElements()).isEqualTo(5);
            assertThat(transactions.getContent()).containsOnly(transaction5, transaction4);
        }

        {
            var pageable = PageRequest.of(1, 2, Sort.by("createdAt").descending());
            var transactions = underTest.findAllByWalletId(wallet.getId(), pageable);

            assertThat(transactions.getTotalElements()).isEqualTo(5);
            assertThat(transactions.getContent()).containsOnly(transaction3, transaction2);
        }

        {
            var pageable = PageRequest.of(2, 2, Sort.by("createdAt").descending());
            var transactions = underTest.findAllByWalletId(wallet.getId(), pageable);

            assertThat(transactions.getTotalElements()).isEqualTo(5);
            assertThat(transactions.getContent()).containsOnly(transaction1);
        }

    }
}