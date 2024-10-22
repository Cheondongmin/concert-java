package com.hhplus.concert.core.interfaces.api.surppot.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonInclude(NON_NULL)
public class ExceptionMessage {

    private String code;
    private String message;
    private Object data;

    // 빈 객체로 반환하기 위한 기본 생성자 추가
    public ExceptionMessage() {
    }

    public ExceptionMessage(Exception exception, HttpStatus httpStatus) {
        this.code = httpStatus.toString();
        this.message = exception.getMessage();
        this.data = null;
    }

    public ExceptionMessage(ExceptionCode exceptionCode, Object data) {
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
        this.data = data;
    }
}