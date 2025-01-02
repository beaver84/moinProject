package com.example.moinproject.config.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IdTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIdType {
    String message() default "유효하지 않은 ID 타입입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

