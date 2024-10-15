package com.hhplus.concert.core.interfaces.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;

public record CommonRes<T>(
        @Schema(description = "반환 결과")
        ResultType resultType,
        @Schema(description = "반환 데이터")
        T data,
        @Schema(description = "반환 메시지", defaultValue = "SUCCESS or error 메시지")
        String message
) {
    @Override
    public String toString() {
        return "{\"CommonRes\":{"
                + "        \"resultType\":\"" + resultType + "\""
                + ",         \"data\":" + data
                + ",         \"message\":\"" + message + "\""
                + "}}";
    }

    public static <T> CommonRes<T> success(T data) {
        return new CommonRes<>(ResultType.SUCCESS, data, "SUCCESS");
    }

    public static CommonRes<?> error(Exception e) {
        return new CommonRes<>(ResultType.FAIL, new HashMap<>(), e.getMessage());
    }
}
