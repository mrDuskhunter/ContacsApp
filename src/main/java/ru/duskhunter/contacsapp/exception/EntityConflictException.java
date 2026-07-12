package ru.duskhunter.contacsapp.exception;

import lombok.Getter;

@Getter
public class EntityConflictException extends RuntimeException {
    private final Object dto;

    public EntityConflictException(String message, Object dto) {
        super(message);
        this.dto = dto;
    }
}