package com.hhplus.concert.app.interfaces.v1.concert.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record SelectSeatRes(
        @Schema(description = "콘서트 좌석 Id", defaultValue = "1")
        long seatId,
        @Schema(description = "콘서트 좌석 번호", defaultValue = "1")
        int position,
        @Schema(description = "콘서트 좌석 가격", defaultValue = "50000")
        long amount,
        @Schema(description = "콘서트 좌석 상태", defaultValue = "AVAILABLE")
        String seatStatus
) {
}
