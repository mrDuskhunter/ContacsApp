package ru.duskhunter.contacsapp.common.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Validator {
    private static jakarta.validation.Validator validator;

    public static <T> List<String> validate(T target) throws ConstraintViolationException {
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