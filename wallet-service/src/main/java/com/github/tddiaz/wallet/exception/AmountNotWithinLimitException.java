package com.github.tddiaz.wallet.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@ResponseErrorCode("AMOUNT_NOT_WITHIN_LIMIT")
public class AmountNotWithinLimitException extends AppException {
    public AmountNotWithinLimitException(String message) {
        super(message, null);
    }
}
