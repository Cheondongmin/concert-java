package com.hhplus.concert.core.domain.payment;

import com.hhplus.concert.core.domain.reservation.ReservationStatus;
import com.hhplus.concert.core.domain.queue.QueueStatus;

public record PaymentConcertResult(
        long paymentAmount,
        ReservationStatus seatStatus,
        QueueStatus queueStatus
) {
}
