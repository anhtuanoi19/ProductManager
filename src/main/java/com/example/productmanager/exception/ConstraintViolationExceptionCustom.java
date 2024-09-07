package com.example.productmanager.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@Getter
@Setter
public class ConstraintViolationExceptionCustom extends ConstraintViolationException {
    public ConstraintViolationExceptionCustom(String message, Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(message, constraintViolations);
    }

    public ConstraintViolationExceptionCustom(Set<? extends ConstraintViolation<?>> constraintViolations) {
        super(constraintViolations);
    }
}