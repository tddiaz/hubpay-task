package com.github.tddiaz.wallet.controller.dto.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidEnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Set<String> acceptedValues;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.acceptedValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }

        return acceptedValues.contains(value);
    }
}
