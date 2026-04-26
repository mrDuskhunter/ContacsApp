package ru.duskhunter.contacsapp.common.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;

import java.util.ArrayList;
import java.util.List;

public class ServerResponseHelper {
    public static <T> ServerResponse<T> response(boolean success, T result, HttpStatus httpStatus,
                                                 List<String> errorMessage) {
        return ServerResponse.<T>builder()
                .success(success)
                .result(result)
                .httpStatus(httpStatus)
                .errorMessages(errorMessage)
                .build();
    }

    public static <T> ResponseEntity<ServerResponse<T>> response(T result, HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body(ServerResponse.<T>builder()
                .success(true)
                .result(result)
                .httpStatus(httpStatus)
                .errorMessages(new ArrayList<>())
                .build());
    }
}
