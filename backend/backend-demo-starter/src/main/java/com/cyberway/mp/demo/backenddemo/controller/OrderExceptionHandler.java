package com.cyberway.mp.demo.backenddemo.controller;

import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(BaseI18nException.class)
    public ResponseEntity<ResponseResult<Void>> handleOrderException(BaseI18nException exception) {
        HttpStatus status = switch (exception.getCode()) {
            case "90000101", "90000201", "90000203", "90000206" -> HttpStatus.CONFLICT;
            case "90000102", "90000202" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status)
                .body(ResponseResult.failResult(
                        exception.getCode(),
                        exception.getMessage(),
                        null));
    }
}
