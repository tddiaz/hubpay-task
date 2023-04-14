package com.github.tddiaz.wallet.service;

import com.github.tddiaz.wallet.controller.dto.GetTransactionsRequestDto;
import com.github.tddiaz.wallet.controller.dto.MoneyDto;
import com.github.tddiaz.wallet.controller.dto.TransactionDto;
import com.github.tddiaz.wallet.exception.NotFoundException;
import com.github.tddiaz.wallet.model.Money;
import com.github.tddiaz.wallet.model.Transaction;
import com.github.tddiaz.wallet.model.Wallet;
import com.github.tddiaz.wallet.repository.TransactionRepository;
import com.github.tddiaz.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GetTransactionsUseCaseTest {

    private WalletRepository walletRepository = mock(WalletRepository.class);

    private TransactionRepository transactionRepository = mock(TransactionRepository.class);

    private GetTransactionsUseCase underTest = new GetTransactionsUseCase(walletRepository, transactionRepository);

    private GetTransactionsRequestDto getTransactionsRequest = new GetTransactionsRequestDto(
        123L, 123L, 0, 10
    );

    @Test
    void givenGetTransactionsRequest_whenExecuteAndWalletIsNotFound_thenThrowError() {
        // given
        when(walletRepository.findByIdAndCustomerId(getTransactionsRequest.walletId(), getTransactionsRequest.customerId()))
            .thenReturn(Optional.empty());

        // when then
        assertThrows(NotFoundException.class, () -> underTest.execute(getTransactionsRequest));
    }

    @Test
    void givenGetTransactionsRequest_whenExecuteAndWalletIsFound_thenResponse() {
        // given
        var wallet = mock(Wallet.class);
        when(wallet.getId()).thenReturn(123L);

        when(walletRepository.findByIdAndCustomerId(getTransactionsRequest.walletId(), getTransactionsRequest.customerId()))
            .thenReturn(Optional.of(wallet));

        var amount = Money.create("GBP", BigDecimal.TEN);
        var depositTransaction = Transaction.createDepositRequest(123L, amount, "REF123");

        var page = new PageImpl(List.of(depositTransaction), PageRequest.of(0, 1), 1);
        when(transactionRepository.findAllByWalletId(eq(123L), any(Pageable.class)))
            .thenReturn(page);

        // when
        var response = underTest.execute(getTransactionsRequest);

        // then
        var pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(transactionRepository).findAllByWalletId(eq(123L), pageRequestArgumentCaptor.capture());

        assertThat(pageRequestArgumentCaptor.getValue()).isEqualTo(
            PageRequest.of(getTransactionsRequest.pageNumber(), getTransactionsRequest.size(), Sort.by("createdAt").descending())
        );

        assertThat(response.numberOfElements()).isEqualTo(1);
        assertThat(response.data()).containsExactly(
            new TransactionDto(
                depositTransaction.getId(),
                depositTransaction.getReferenceId(),
                depositTransaction.getType().name(),
                depositTransaction.getEntry().name(),
                depositTransaction.getCreatedAt(),
                new MoneyDto(depositTransaction.getAmount().getCurrency(), depositTransaction.getAmount().getAmount())
            )
        );
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.pageNumber()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(1);
    }

}