package ru.mihaliks.finance.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import ru.mihaliks.finance.common.api.ApiError;
import ru.mihaliks.finance.common.exception.ApiException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApi(ApiException exception, ServerWebExchange exchange) {
        return response(exception.status(), exception.code(), exception.getMessage(), exchange, Map.of());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> handleValidation(WebExchangeBindException exception, ServerWebExchange exchange) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError error : exception.getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Некорректные входные данные", exchange, fields);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception, ServerWebExchange exchange) {
        log.error("Unexpected request error at {}", exchange.getRequest().getPath(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Внутренняя ошибка сервера", exchange, Map.of());
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String code,
            String message,
            ServerWebExchange exchange,
            Map<String, String> fields
    ) {
        ApiError body = new ApiError(Instant.now(), status.value(), code, message,
                exchange.getRequest().getPath().value(), fields);
        return ResponseEntity.status(status).body(body);
    }
}
