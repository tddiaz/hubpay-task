package com.github.tddiaz.wallet.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
@ResponseErrorCode("NOT_FOUND")
public class NotFoundException extends AppException {
    public NotFoundException(String message) {
        super(message, null);
    }
}
