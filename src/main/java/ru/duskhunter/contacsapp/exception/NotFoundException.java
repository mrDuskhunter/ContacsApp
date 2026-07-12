package ru.duskhunter.contacsapp.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final Object dto;

    public NotFoundException(String message, Object dto) {
        super(message);
        this.dto = dto;
    }
}