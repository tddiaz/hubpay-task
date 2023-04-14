package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.model.Money;
import com.github.tddiaz.wallet.model.Wallet;
import com.github.tddiaz.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetWalletDetailsUseCaseTest {

    private WalletRepository walletRepository = mock(WalletRepository.class);

    private GetWalletDetailsUseCase underTest = new GetWalletDetailsUseCase(walletRepository);

    @Test
    void givenWalletIdAndCustomerId_whenExecuteAndWalletNotFound_thenThrowError() {
        // given
        when(walletRepository.findByIdAndCustomerId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // when then
        assertThrows(NotFoundException.class, () -> underTest.execute(123L, 123L));
    }

    @Test
    void givenWalletIdAndCustomerId_whenExecuteAndWalletIsFound_thenReturnWalletDetails() {
        // given
        var wallet = Wallet.initialize(123L, "GBP");
        wallet.deposit(Money.create("GBP", BigDecimal.TEN));
        wallet.withdraw(Money.create("GBP", BigDecimal.ONE));

        when(walletRepository.findByIdAndCustomerId(anyLong(), anyLong())).thenReturn(Optional.of(wallet));

        // when
        var walletDetailsResponse = underTest.execute(123L, 123L);

        // then
        assertThat(walletDetailsResponse.walletId()).isEqualTo(wallet.getId());
        assertThat(walletDetailsResponse.customerId()).isEqualTo(123L);
        assertThat(walletDetailsResponse.totalBalance()).isEqualTo(new MoneyDto("GBP", BigDecimal.valueOf(9L)));
        assertThat(walletDetailsResponse.amountOnHold()).isEqualTo(new MoneyDto("GBP", BigDecimal.ONE));
    }

}