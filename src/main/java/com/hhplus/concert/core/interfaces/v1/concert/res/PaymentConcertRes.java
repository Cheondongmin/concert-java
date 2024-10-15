package com.hhplus.concert.core.interfaces.v1.concert.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentConcertRes(
        @Schema(description = "지불 된 요금", defaultValue = "50000")
        int paymentAmount,
        @Schema(description = "콘서트 좌석 상태", defaultValue = "RESERVED")
        String seatStatus,
        @Schema(description = "예약 대기상태(큐상태)", defaultValue = "DONE")
        String queueStatus
) {
}
