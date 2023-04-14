package com.github.tddiaz.wallet.controller;

import com.github.tddiaz.wallet.BaseControllerIT;
import com.github.tddiaz.wallet.controller.dto.GetTransactionsRequestDto;
import com.github.tddiaz.wallet.controller.dto.GetTransactionsResponseDto;
import com.github.tddiaz.wallet.service.GetTransactionsUseCase;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(TransactionController.class)
class TransactionControllerIT extends BaseControllerIT {

    @MockBean
    private GetTransactionsUseCase getTransactionsUseCase;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    void givenEmptyParams_whenGetTransactions_thenReturnErrorResponse() {
        RestAssuredMockMvc
            .given()
            .contentType(ContentType.JSON)
            .when()
            .get("/api/v1/transactions/all")
            .then()
            .status(HttpStatus.BAD_REQUEST);
    }

    @Test
    void givenValid_whenGetTransactions_thenReturnSuccessResponse() {
        when(getTransactionsUseCase.execute(any(GetTransactionsRequestDto.class)))
            .thenReturn(new GetTransactionsResponseDto(
                0, 0, 0, 0, Collections.emptyList()
            ));

        RestAssuredMockMvc
            .given()
            .param("walletId", 123)
            .param("customerId", 123)
            .param("pageNumber", 0)
            .param("size", 10)
            .contentType(ContentType.JSON)
            .when()
            .get("/api/v1/transactions/all")
            .then()
            .status(HttpStatus.OK)
            .body("data", hasSize(0))
            .body("totalCount", equalTo(0))
            .body("totalPages", equalTo(0))
            .body("pageNumber", equalTo(0))
            .body("numberOfElements", equalTo(0));

        var getTransactionRequestDtoArgumentCaptor = ArgumentCaptor.forClass(GetTransactionsRequestDto.class);
        verify(getTransactionsUseCase).execute(getTransactionRequestDtoArgumentCaptor.capture());

        var getTransactionRequestDto = getTransactionRequestDtoArgumentCaptor.getValue();
        assertThat(getTransactionRequestDto.walletId()).isEqualTo(123);
        assertThat(getTransactionRequestDto.customerId()).isEqualTo(123);
        assertThat(getTransactionRequestDto.pageNumber()).isEqualTo(0);
        assertThat(getTransactionRequestDto.size()).isEqualTo(10);
    }
}