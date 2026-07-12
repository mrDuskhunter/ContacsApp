package ru.duskhunter.contacsapp.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.exception.EntityConflictException;
import ru.duskhunter.contacsapp.exception.InternalServerException;
import ru.duskhunter.contacsapp.exception.NotFoundException;
import ru.duskhunter.contacsapp.exception.ValidationException;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    //global exception handler
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServerResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .toList();
        return ServerResponseHelper.response(false, null, HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ServerResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        return ServerResponseHelper.response(false, null, HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServerResponse<Object> handleUnexpected(Exception ex) {
        return ServerResponseHelper.response(false, null, HttpStatus.INTERNAL_SERVER_ERROR,
                List.of("Unexpected server error"));
    }

    //local exception handler
    @ExceptionHandler(EntityConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ServerResponse<Object> handleEntityConflict(EntityConflictException ex) {
        return ServerResponseHelper.response(false, ex.getDto(), HttpStatus.CONFLICT, List.of(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ServerResponse<Object> handleNotFound(NotFoundException ex) {
        return ServerResponseHelper.response(false, ex.getDto(), HttpStatus.NOT_FOUND, List.of(ex.getMessage()));
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ServerResponse<Object> handleInternalServerException(InternalServerException ex) {
        return ServerResponseHelper.response(false, ex.getDto(), HttpStatus.INTERNAL_SERVER_ERROR, List.of(ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ServerResponse<Object> handleValidationException(ValidationException ex) {
        return ServerResponseHelper.response(false, ex.getDto(), HttpStatus.BAD_REQUEST, ex.getErrors());
    }
}