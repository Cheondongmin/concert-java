package com.hhplus.concert.core.interfaces.api.exception;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;

@Getter
public class ApiException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    private final LogLevel logLevel;

    private final Object data;

    public ApiException(ExceptionCode exceptionCode, LogLevel logLevel) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.logLevel = logLevel;
        this.data = null;
    }
}
