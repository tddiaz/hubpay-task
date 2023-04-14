package com.github.tddiaz.wallet.controller.dto.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Currency;
import java.util.stream.Collectors;

public class ValidCurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        var currencies = Currency.getAvailableCurrencies().stream().map(Currency::getCurrencyCode).collect(Collectors.toSet());

        return currencies.contains(value);
    }
}
