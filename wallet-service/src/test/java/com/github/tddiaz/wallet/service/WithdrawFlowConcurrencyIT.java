package com.github.tddiaz.wallet.service;

import com.github.f4b6a3.tsid.TsidCreator;
import com.github.tddiaz.wallet.TestDatasourceContextInitializer;
import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto;
import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto.BankingStatus;
import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.controller.dto.WithdrawRequestDto;
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
public class WithdrawFlowConcurrencyIT {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CreateWithdrawOrderUseCase createWithdrawOrderUseCase;

    @Autowired
    private SettleTransactionUseCase settleTransactionUseCase;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * Given:
     *  - a wallet with balance of 100 GBP
     *  - 10 pending withdraw order with requests having withdraw amount of 10 GBP each.
     *
     *  Expected Result:
     *  - total balance should be 0 GBP
     *  - balance on hold should be 100 GBP
     */
    @Test
    public void givenConcurrentWithdrawOrderRequest_whenGetWalletBalance_shouldReturnExpectedCorrectTotalBalanceAndBalanceOnHold() {
        // given
        var expectedTotalBalance = Money.create("GBP", new BigDecimal("0.0000"));
        var expectedBalanceOnHold = Money.create("GBP", new BigDecimal("100.0000"));

        var customerId = TsidCreator.getTsid().toLong();

        // initialize wallet with balance of 100 GBP
        var wallet = Wallet.initialize(customerId, "GBP");
        wallet.deposit(Money.create("GBP", BigDecimal.valueOf(100L)));

        walletRepository.save(wallet);

        var savedWallet = transactionTemplate.execute(action -> walletRepository.findById(wallet.getId()).get());
        assertThat(savedWallet.getBalance()).isEqualTo(Money.create("GBP", new BigDecimal("100.0000")));

        var withdrawAmount = Money.create("GBP", BigDecimal.TEN);

        // when
        Stream.of(
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawOrderRequest(wallet.getId(), customerId, withdrawAmount), executorService)
        ).map(CompletableFuture::join).collect(Collectors.toList());

        // then
        var updatedWallet = transactionTemplate.execute(action -> walletRepository.findById(wallet.getId()).get());

        assertThat(updatedWallet.getBalance()).isEqualTo(expectedTotalBalance);
        assertThat(updatedWallet.getBalanceOnHold()).isEqualTo(expectedBalanceOnHold);
    }

    /**
     * Given:
     *  - a wallet with balance of 100 GBP
     *  - 10 complete successful withdraw order with requests having withdraw amount of 10 GBP each.
     *
     *  Expected Result:
     *  - total balance should be 0 GBP
     *  - balance on hold should be 0 GBP
     */
    @Test
    public void givenConcurrentSuccessWithdrawOrderCompleteFlow_whenGetWalletBalance_shouldReturnCorrectTotalBalanceAndBalanceOnHold() {
        // given
        var expectedTotalBalance = Money.create("GBP", new BigDecimal("0.0000"));
        var expectedBalanceOnHold = Money.create("GBP", new BigDecimal("0.0000"));

        var customerId = TsidCreator.getTsid().toLong();

        // initialize wallet with balance of 100 GBP
        var wallet = Wallet.initialize(customerId, "GBP");
        wallet.deposit(Money.create("GBP", BigDecimal.valueOf(100L)));

        walletRepository.save(wallet);

        var savedWallet = transactionTemplate.execute(action -> walletRepository.findById(wallet.getId()).get());
        assertThat(savedWallet.getBalance()).isEqualTo(Money.create("GBP", new BigDecimal("100.0000")));

        var withdrawAmount = Money.create("GBP", BigDecimal.TEN);

        // when
        Stream.of(
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.SUCCESS), executorService)
        ).map(CompletableFuture::join).collect(Collectors.toList());

        // then
        var updatedWallet = transactionTemplate.execute(action -> walletRepository.findById(wallet.getId()).get());

        assertThat(updatedWallet.getBalance()).isEqualTo(expectedTotalBalance);
        assertThat(updatedWallet.getBalanceOnHold()).isEqualTo(expectedBalanceOnHold);
    }

    /**
     * Given:
     *  - a wallet with balance of 100 GBP
     *  - 10 complete failed withdraw order with requests having withdraw amount of 10 GBP each.
     *
     *  Expected Result:
     *  - total balance should be 100 GBP
     *  - balance on hold should be 0 GBP
     */
    @Test
    public void givenConcurrentFailedWithdrawOrderCompleteFlow_whenGetWalletBalance_shouldReturnCorrectTotalBalanceAndBalanceOnHold() {
        // given
        var expectedTotalBalance = Money.create("GBP", new BigDecimal("100.0000"));
        var expectedBalanceOnHold = Money.create("GBP", new BigDecimal("0.0000"));

        var customerId = TsidCreator.getTsid().toLong();

        // initialize wallet with balance of 100 GBP
        var wallet = Wallet.initialize(customerId, "GBP");
        wallet.deposit(Money.create("GBP", BigDecimal.valueOf(100L)));

        walletRepository.save(wallet);

        var savedWallet = transactionTemplate.execute(action -> walletRepository.findById(wallet.getId()).get());
        assertThat(savedWallet.getBalance()).isEqualTo(Money.create("GBP", new BigDecimal("100.0000")));

        var withdrawAmount = Money.create("GBP", BigDecimal.TEN);

        // when
        Stream.of(
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService),
            CompletableFuture.supplyAsync(() -> executeWithdrawCompleteFlow(wallet.getId(), customerId, withdrawAmount, BankingStatus.FAILED), executorService)
        ).map(CompletableFuture::join).collect(Collectors.toList());

        // then
        var updatedWallet = transactionTemplate.execute(action -> walletRepository.findById(wallet.getId()).get());

        assertThat(updatedWallet.getBalance()).isEqualTo(expectedTotalBalance);
        assertThat(updatedWallet.getBalanceOnHold()).isEqualTo(expectedBalanceOnHold);
    }



    private int executeWithdrawOrderRequest(Long walletId, Long customerId, Money withdrawAmount) {
        transactionTemplate.execute(action ->
            createWithdrawOrderUseCase.execute(
                new WithdrawRequestDto(UUID.randomUUID().toString(), walletId, customerId, new MoneyDto(withdrawAmount.getCurrency(), withdrawAmount.getAmount()))
            )
        );
        return 0;
    }

    private int executeWithdrawCompleteFlow(Long walletId, Long customerId, Money withdrawAmount, BankingStatus bankingStatus) {
        var referenceId = UUID.randomUUID().toString();

        transactionTemplate.execute(action ->
            createWithdrawOrderUseCase.execute(
                new WithdrawRequestDto(referenceId, walletId, customerId, new MoneyDto(withdrawAmount.getCurrency(), withdrawAmount.getAmount()))
            )
        );

        transactionTemplate.execute(action -> {
            settleTransactionUseCase.execute(new BankTransactionStatusRequestDto(referenceId, bankingStatus.name()));
            return null;
        });

        return 0;
    }
}
