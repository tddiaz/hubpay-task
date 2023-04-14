package com.github.tddiaz.wallet.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@ResponseErrorCode("CURRENCY_MISMATCH")
public class CurrencyMismatchException extends AppException {
    public CurrencyMismatchException(String message) {
        super(message, null);
    }
}
