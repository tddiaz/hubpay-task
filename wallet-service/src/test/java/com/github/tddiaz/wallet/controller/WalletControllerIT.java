package com.github.tddiaz.wallet.controller;

import com.github.tddiaz.wallet.BaseControllerIT;
import com.github.tddiaz.wallet.controller.dto.*;
import com.github.tddiaz.wallet.service.CreateDepositOrderUseCase;
import com.github.tddiaz.wallet.service.CreateWithdrawOrderUseCase;
import com.github.tddiaz.wallet.service.GetWalletDetailsUseCase;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(WalletController.class)
class WalletControllerIT extends BaseControllerIT {

    @MockBean
    private CreateWithdrawOrderUseCase createWithdrawOrderUseCase;

    @MockBean
    private CreateDepositOrderUseCase createDepositOrderUseCase;

    @MockBean
    private GetWalletDetailsUseCase getWalletDetailsUseCase;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Nested
    class DepositApiTests {

        @Test
        void givenRequestWithNullFields_whenDeposit_thenReturnErrorResponse() {
            List<ApiFieldError> fieldErrors = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new DepositRequestDto(null, null, null, null))
                .when()
                .post("/api/v1/wallet/deposit")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().path("fieldErrors");

            assertThat(fieldErrors).hasSize(4);
            assertThat(fieldErrors).extracting("code").containsOnly("REQUIRED_NOT_BLANK", "REQUIRED_NOT_NULL");
            assertThat(fieldErrors).extracting("property").containsOnly("referenceId", "walletId", "customerId", "amount");
        }

        @Test
        void givenRequestWithAmountWithNullFields_whenDeposit_thenReturnErrorResponse() {
            List<ApiFieldError> fieldErrors = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new DepositRequestDto("REF123", 123L, 123L, new MoneyDto(null, null)))
                .when()
                .post("/api/v1/wallet/deposit")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().path("fieldErrors");

            assertThat(fieldErrors).hasSize(2);
            assertThat(fieldErrors).extracting("code").containsOnly("REQUIRED_NOT_BLANK", "REQUIRED_NOT_NULL");
            assertThat(fieldErrors).extracting("property").containsOnly("amount.currency", "amount.value");
        }

        @Test
        void givenRequestWithAmountWithInvalidCurrency_whenDeposit_thenReturnErrorResponse() {
            List<ApiFieldError> fieldErrors = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new DepositRequestDto("REF123", 123L, 123L, new MoneyDto("FOO", BigDecimal.TEN)))
                .when()
                .post("/api/v1/wallet/deposit")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().path("fieldErrors");

            assertThat(fieldErrors).hasSize(1);
            assertThat(fieldErrors).extracting("code").containsOnly("INVALID_CURRENCY_CODE");
            assertThat(fieldErrors).extracting("property").containsOnly("amount.currency");
        }

        @Test
        void givenValidRequest_whenDeposit_thenReturnSuccessResponse() {
            when(createDepositOrderUseCase.execute(ArgumentMatchers.any(DepositRequestDto.class)))
                .thenReturn(new DepositResponseDto(1234L, "SUCCESS"));

            RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new DepositRequestDto("REF123", 123L, 123L, new MoneyDto("GBP", BigDecimal.TEN)))
                .when()
                .post("/api/v1/wallet/deposit")
                .then()
                .status(HttpStatus.OK)
                .body("transactionId", equalTo(1234))
                .body("status", equalTo("SUCCESS"));

            var depositRequestDtoArgumentCaptor = ArgumentCaptor.forClass(DepositRequestDto.class);
            verify(createDepositOrderUseCase).execute(depositRequestDtoArgumentCaptor.capture());

            var depositRequestDto = depositRequestDtoArgumentCaptor.getValue();
            assertThat(depositRequestDto.referenceId()).isEqualTo("REF123");
            assertThat(depositRequestDto.walletId()).isEqualTo(123L);
            assertThat(depositRequestDto.customerId()).isEqualTo(123L);
            assertThat(depositRequestDto.amount()).isEqualTo(new MoneyDto("GBP", BigDecimal.TEN));
        }
    }

    @Nested
    class WithdrawApiTests {

        @Test
        void givenRequestWithNullFields_whenWithdraw_thenReturnErrorResponse() {
            List<ApiFieldError> fieldErrors = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new WithdrawRequestDto(null, null, null, null))
                .when()
                .post("/api/v1/wallet/withdraw")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().path("fieldErrors");

            assertThat(fieldErrors).hasSize(4);
            assertThat(fieldErrors).extracting("code").containsOnly("REQUIRED_NOT_BLANK", "REQUIRED_NOT_NULL");
            assertThat(fieldErrors).extracting("property").containsOnly("referenceId", "walletId", "customerId", "amount");
        }

        @Test
        void givenRequestWithAmountWithNullFields_whenWithdraw_thenReturnErrorResponse() {
            List<ApiFieldError> fieldErrors = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new WithdrawRequestDto("REF123", 123L, 123L, new MoneyDto(null, null)))
                .when()
                .post("/api/v1/wallet/withdraw")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().path("fieldErrors");

            assertThat(fieldErrors).hasSize(2);
            assertThat(fieldErrors).extracting("code").containsOnly("REQUIRED_NOT_BLANK", "REQUIRED_NOT_NULL");
            assertThat(fieldErrors).extracting("property").containsOnly("amount.currency", "amount.value");
        }

        @Test
        void givenRequestWithAmountWithInvalidCurrency_whenWithdraw_thenReturnErrorResponse() {
            List<ApiFieldError> fieldErrors = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new DepositRequestDto("REF123", 123L, 123L, new MoneyDto("FOO", BigDecimal.TEN)))
                .when()
                .post("/api/v1/wallet/withdraw")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .body("code", equalTo("VALIDATION_FAILED"))
                .extract().path("fieldErrors");

            assertThat(fieldErrors).hasSize(1);
            assertThat(fieldErrors).extracting("code").containsOnly("INVALID_CURRENCY_CODE");
            assertThat(fieldErrors).extracting("property").containsOnly("amount.currency");
        }

        @Test
        void givenValidRequest_whenWithdraw_thenReturnSuccessResponse() {
            when(createWithdrawOrderUseCase.execute(ArgumentMatchers.any(WithdrawRequestDto.class)))
                .thenReturn(new WithdrawResponseDto(1234L, "SUCCESS"));

            RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(new WithdrawRequestDto("REF123", 123L, 123L, new MoneyDto("GBP", BigDecimal.TEN)))
                .when()
                .post("/api/v1/wallet/withdraw")
                .then()
                .status(HttpStatus.OK)
                .body("transactionId", equalTo(1234))
                .body("status", equalTo("SUCCESS"));

            var withdrawRequestDtoArgumentCaptor = ArgumentCaptor.forClass(WithdrawRequestDto.class);
            verify(createWithdrawOrderUseCase).execute(withdrawRequestDtoArgumentCaptor.capture());

            var withdrawRequestDto = withdrawRequestDtoArgumentCaptor.getValue();
            assertThat(withdrawRequestDto.referenceId()).isEqualTo("REF123");
            assertThat(withdrawRequestDto.walletId()).isEqualTo(123L);
            assertThat(withdrawRequestDto.customerId()).isEqualTo(123L);
            assertThat(withdrawRequestDto.amount()).isEqualTo(new MoneyDto("GBP", BigDecimal.TEN));
        }
    }

    @Nested
    class GetWalletDetailsApiTests {

        @Test
        void givenWalletIdAndCustomerId_whenGetWalletDetails_thenReturnSuccessResponse() {
            var expectedResponse = new GetWalletDetailsResponseDto(
                123L,
                123L,
                new MoneyDto("GBP", BigDecimal.TEN),
                new MoneyDto("GBP", BigDecimal.ONE)
            );
            when(getWalletDetailsUseCase.execute(anyLong(), anyLong())).thenReturn(expectedResponse);

            GetWalletDetailsResponseDto actualResponse = RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/wallet/123/customer/123")
                .then()
                .status(HttpStatus.OK)
                .extract().response().body().as(GetWalletDetailsResponseDto.class);

            assertThat(actualResponse).isEqualTo(expectedResponse);

            verify(getWalletDetailsUseCase).execute(eq(123L), eq(123L));
        }
    }
}