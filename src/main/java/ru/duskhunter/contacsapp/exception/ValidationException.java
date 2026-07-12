package ru.duskhunter.contacsapp.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {
    private final Object dto;
    private final List<String> errors;

    public ValidationException(List<String> errors, Object dto) {
        super("Validation failed");
        this.errors = errors;
        this.dto = dto;
    }
}