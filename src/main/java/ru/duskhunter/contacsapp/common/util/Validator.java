package ru.duskhunter.contacsapp.common.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class Validator {
    private final jakarta.validation.Validator validator;

    @Autowired
    public Validator(jakarta.validation.Validator validator) {
        this.validator = validator;
    }

    public <T> List<String> validate(T target) throws ConstraintViolationException {
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        List<String> errors = new ArrayList<>();
        if (!violations.isEmpty()) {
            errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
        }
        return errors;
    }
}