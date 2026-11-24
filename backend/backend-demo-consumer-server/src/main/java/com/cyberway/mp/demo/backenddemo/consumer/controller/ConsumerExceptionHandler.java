package com.cyberway.mp.demo.backenddemo.consumer.controller;

import com.cyberway.mp.bc.common.api.exception.BaseException;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ConsumerExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ResponseResult<Void>> handleRemoteFailure(BaseException exception) {
        HttpStatus status = switch (exception.getCode()) {
            case "90000101" -> HttpStatus.CONFLICT;
            case "90000102" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_GATEWAY;
        };
        return ResponseEntity.status(status)
                .body(ResponseResult.failResult(
                        exception.getCode(),
                        exception.getMessage(),
                        null));
    }
}
