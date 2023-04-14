package com.github.tddiaz.wallet.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@ResponseErrorCode("INVALID_AMOUNT")
public class InvalidAmountException extends AppException {
    public InvalidAmountException(String message) {
        super(message, null);
    }
}
