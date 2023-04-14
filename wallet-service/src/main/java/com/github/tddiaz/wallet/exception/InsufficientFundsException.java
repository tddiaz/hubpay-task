package com.github.tddiaz.wallet.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@ResponseErrorCode("INSUFFICIENT_FUNDS")
public class InsufficientFundsException extends AppException {
    public InsufficientFundsException(String message) {
        super(message, null);
    }
}
