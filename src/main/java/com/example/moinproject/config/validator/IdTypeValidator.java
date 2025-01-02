package com.example.moinproject.config.validator;

import com.example.moinproject.domain.enums.IdType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;

public class IdTypeValidator implements ConstraintValidator<ValidIdType, IdType> {
    @Override
    public boolean isValid(IdType value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return EnumSet.allOf(IdType.class).contains(value);
    }
}
