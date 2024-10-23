package com.hhplus.concert.core.interfaces.api.common;

import com.hhplus.concert.core.interfaces.api.support.exception.ExceptionCode;
import com.hhplus.concert.core.interfaces.api.support.exception.ExceptionMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

import static com.hhplus.concert.core.interfaces.api.common.ResultType.FAIL;
import static com.hhplus.concert.core.interfaces.api.common.ResultType.SUCCESS;

public record CommonRes<T>(
        @Schema(description = "반환 결과")
        ResultType resultType,
        @Schema(description = "반환 데이터")
        T data,
        @Schema(description = "반환 메시지", defaultValue = "SUCCESS or error 메시지")
        ExceptionMessage exception
) {
    @Override
    public String toString() {
        return "{\"CommonRes\":{"
                + "        \"resultType\":\"" + resultType + "\""
                + ",         \"data\":" + data
                + ",         \"exception\":\"" + exception + "\""
                + "}}";
    }

    public static <T> CommonRes<T> success(T data) {
        return new CommonRes<>(SUCCESS, data, new ExceptionMessage());
    }

    public static CommonRes<?> error(Exception error, HttpStatus status) {
        return new CommonRes<>(FAIL, new HashMap<>(), new ExceptionMessage(error, status));
    }

    public static CommonRes<?> error(ExceptionCode error, Object errorData) {
        return new CommonRes<>(FAIL, new HashMap<>(), new ExceptionMessage(error, errorData));
    }
}
