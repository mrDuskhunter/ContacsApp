package ru.duskhunter.contacsapp.exception;

import lombok.Getter;

@Getter
public class InternalServerException extends RuntimeException {
    private final Object dto;

    public InternalServerException(String message, Object dto) {
        super(message);
        this.dto = dto;
    }
}