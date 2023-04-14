package com.github.tddiaz.wallet.controller;

import com.github.tddiaz.wallet.BaseControllerIT;
import com.github.tddiaz.wallet.controller.dto.BankTransactionStatusRequestDto;
import com.github.tddiaz.wallet.service.SettleTransactionUseCase;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@WebMvcTest(BankCallbackController.class)
class BankCallbackControllerIT extends BaseControllerIT {

    @MockBean
    private SettleTransactionUseCase settleTransactionUseCase;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    void givenRequestWithNullFields_whenNotifyTransferStatus_thenReturnErrorResponse() {
        List<ApiFieldError> fieldErrors = RestAssuredMockMvc
            .given()
            .contentType(ContentType.JSON)
            .body(new BankTransactionStatusRequestDto(null, null))
            .when()
            .post("/api/v1/external/banking/notify-transfer-status")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .body("code", equalTo("VALIDATION_FAILED"))
            .extract().path("fieldErrors");

        assertThat(fieldErrors).hasSize(2);
        assertThat(fieldErrors).extracting("code").containsOnly("REQUIRED_NOT_BLANK");
        assertThat(fieldErrors).extracting("property").containsOnly("referenceId", "status");
    }

    @Test
    void givenRequestWithInvalidStatus_whenNotifyTransferStatus_thenReturnErrorResponse() {
        List<ApiFieldError> fieldErrors = RestAssuredMockMvc
            .given()
            .contentType(ContentType.JSON)
            .body(new BankTransactionStatusRequestDto("REF123", "REVERSED"))
            .when()
            .post("/api/v1/external/banking/notify-transfer-status")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .body("code", equalTo("VALIDATION_FAILED"))
            .extract().path("fieldErrors");

        assertThat(fieldErrors).hasSize(1);
        assertThat(fieldErrors).extracting("code").containsOnly("INVALID_ENUM");
        assertThat(fieldErrors).extracting("property").containsOnly("status");
    }

    @Test
    void givenValidRequest_whenNotifyTransferStatus_thenReturnSuccess() {
        doNothing().when(settleTransactionUseCase).execute(any(BankTransactionStatusRequestDto.class));

        RestAssuredMockMvc
            .given()
            .contentType(ContentType.JSON)
            .body(new BankTransactionStatusRequestDto("REF123", "SUCCESS"))
            .when()
            .post("/api/v1/external/banking/notify-transfer-status")
            .then()
            .status(HttpStatus.OK);

        var bankTransactionStatusRequestDtoArgumentCaptor = ArgumentCaptor.forClass(BankTransactionStatusRequestDto.class);
        verify(settleTransactionUseCase).execute(bankTransactionStatusRequestDtoArgumentCaptor.capture());

        var bankTransactionStatusRequestDto = bankTransactionStatusRequestDtoArgumentCaptor.getValue();
        assertThat(bankTransactionStatusRequestDto.referenceId()).isEqualTo("REF123");
        assertThat(bankTransactionStatusRequestDto.status()).isEqualTo("SUCCESS");
    }
}