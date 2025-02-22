package com.hhplus.concert.core.interfaces.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionCode {
    E001("001", "정상적인 프로세스로 접근하지 않았습니다. 다시 대기열에 접근해주세요.", HttpStatus.BAD_REQUEST),
    E002("002", "죄송합니다. 해당 콘서트는 모든 좌석이 매진된 콘서트입니다.", HttpStatus.BAD_REQUEST),
    E003("003", "대기열 상태가 활성상태가 아닙니다.", HttpStatus.BAD_REQUEST),
    E004("004", "해당 좌석은 예약할 수 없는 상태 입니다.", HttpStatus.BAD_REQUEST),
    E005("005", "유저의 잔액이 부족합니다 잔액을 다시 확인해주세요!", HttpStatus.BAD_REQUEST),

    E403("403", "접근할 수 없습니다.", HttpStatus.FORBIDDEN),
    E404("404", "데이터를 조회할 수 없습니다.", HttpStatus.NOT_FOUND),
    E500("500", "알 수 없는 문제가 발생했습니다. 관리자에게 문의 부탁드립니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
