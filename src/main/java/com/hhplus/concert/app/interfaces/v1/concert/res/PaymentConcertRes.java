package com.hhplus.concert.app.interfaces.v1.concert.res;

public record PaymentConcertRes(
        String message,
        int paymentAmount,
        String seatStatus,
        String queueStatus
) {
}
