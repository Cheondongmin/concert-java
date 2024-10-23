package com.hhplus.concert.core.interfaces.api.support;

import com.hhplus.concert.core.interfaces.api.common.CommonRes;
import com.hhplus.concert.core.interfaces.api.support.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.support.exception.ExceptionCode;
import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class ApiControllerAdvice {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<CommonRes<?>> handleCoreApiException(ApiException e) {
        switch (e.getLogLevel()) {
            case ERROR -> log.error("ApiException : {}", e.getMessage(), e);
            case WARN -> log.warn("ApiException : {}", e.getMessage(), e);
            default -> log.info("ApiException : {}", e.getMessage(), e);
        }

        HttpStatus httpStatus;
        switch (e.getExceptionCode().getCode()) {
            case "403" -> httpStatus = FORBIDDEN;
            case "404" -> httpStatus = NOT_FOUND;
            default -> httpStatus = INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(CommonRes.error(e.getExceptionCode(), e.getData()), httpStatus);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonRes<?>> handleCoreApiException(IllegalArgumentException e) {
        log.info("IllegalArgumentException : {}", e.getMessage(), e);
        return new ResponseEntity<>(CommonRes.error(e, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<CommonRes<?>> handleCoreApiException(NullPointerException e) {
        log.info("NullPointerException : {}", e.getMessage(), e);
        return new ResponseEntity<>(CommonRes.error(e, INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonRes<?>> handleException(Exception e) {
        log.error("Exception : {}", e.getMessage(), e);
        return new ResponseEntity<>(CommonRes.error(ExceptionCode.E500, INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }
}
