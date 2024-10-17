package com.hhplus.concert.core.interfaces.v1.concert.res;

import com.hhplus.concert.core.domain.concert.PaymentConcertResult;
import com.hhplus.concert.core.domain.concert.ReservationStatus;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentConcertRes(
        @Schema(description = "지불 된 요금", defaultValue = "50000")
        long paymentAmount,
        @Schema(description = "콘서트 좌석 상태", defaultValue = "RESERVED")
        ReservationStatus seatStatus,
        @Schema(description = "예약 대기상태(큐상태)", defaultValue = "DONE")
        QueueStatus queueStatus
) {
    public static PaymentConcertRes of(PaymentConcertResult paymentConcertResult) {
        return new PaymentConcertRes(
                paymentConcertResult.paymentAmount(),
                paymentConcertResult.seatStatus(),
                paymentConcertResult.queueStatus()
        );
    }
}
