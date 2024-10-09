package com.hhplus.concert.app.interfaces.common;

import java.util.HashMap;

public record CommonRes<T>(
        ResultType resultType,
        T data,
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
        return new CommonRes<>(ResultType.SUCCESS, data, "success");
    }

    public static CommonRes<?> error(Exception e) {
        return new CommonRes<>(ResultType.FAIL, new HashMap<>(), e.getMessage());
    }
}
