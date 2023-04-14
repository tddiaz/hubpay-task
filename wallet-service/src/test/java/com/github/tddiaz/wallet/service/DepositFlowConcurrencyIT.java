package com.github.tddiaz.wallet.service;

import com.github.f4b6a3.tsid.TsidCreator;
import com.github.tddiaz.wallet.TestDatasourceContextInitializer;
import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto;
import com.github.tddiaz.wallet.controller.dto.DepositRequestDto;
import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.model.Money;
import com.github.tddiaz.wallet.model.Wallet;
import com.github.tddiaz.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    initializers = {TestDatasourceContextInitializer.class}
)
public class DepositFlowConcurrencyIT {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CreateDepositOrderUseCase createDepositOrderUseCase;

    @Autowired
    private SettleTransactionUseCase settleTransactionUseCase;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Given:
     *  - a wallet with zero balance
     *  - 10 concurrent complete deposit order flow with requests having deposit amount of 10 GBP each.
     *
     *  Expected Result:
     *  - total balance should be 100 GBP
     */
    @Test
    public void givenConcurrentDepositOrder_whenGetWalletBalance_shouldReturnCorrectTotalBalance() {
        // given
        var expectedTotalBalance = Money.create("GBP", new BigDecimal("100.0000"));

        var customerId = TsidCreator.getTsid().toLong();
        // initialize wallet with zero balance
        var wallet = Wallet.initialize(customerId, "GBP");

        walletRepository.save(wallet);

        var depositAmount = Money.create("GBP", BigDecimal.TEN);

        // when
        Stream.of(
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeDepositFlow(wallet.getId(), customerId, depositAmount), executorService)
        ).map(CompletableFuture::join).collect(Collectors.toList());

        // then
        var updatedWallet = transactionTemplate.execute(action -> walletRepository.findById(wallet.getId()).get());

        assertThat(updatedWallet.getBalance()).isEqualTo(expectedTotalBalance);
    }

    private int executeDepositFlow(Long walletId, Long customerId, Money depositAmount) {
        var referenceId = UUID.randomUUID().toString();

        transactionTemplate.execute(action ->
            createDepositOrderUseCase.execute(
                new DepositRequestDto(referenceId, walletId, customerId, new MoneyDto(depositAmount.getCurrency(), depositAmount.getAmount()))
            )
        );

        transactionTemplate.execute(action -> {
            settleTransactionUseCase.execute(new BankTransactionStatusRequestDto(referenceId, "SUCCESS"));
            return null;
        });

        return 0;
    }
}
