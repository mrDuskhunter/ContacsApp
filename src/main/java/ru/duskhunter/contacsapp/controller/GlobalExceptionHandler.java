package ru.duskhunter.contacsapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ServerResponse<Void> handleValidationException(MethodArgumentNotValidException ex){

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .toList();

        return ServerResponseHelper.response(false,null, HttpStatus.CONFLICT,errors);
    }
}