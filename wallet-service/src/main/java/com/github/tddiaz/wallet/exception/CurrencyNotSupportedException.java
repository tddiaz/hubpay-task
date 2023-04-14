package com.github.tddiaz.wallet.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@ResponseErrorCode("CURRENCY_NOT_SUPPORTED")
public class CurrencyNotSupportedException extends AppException {
    public CurrencyNotSupportedException(String message) {
        super(message, null);
    }
}
