package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.domain.queue.QueueStatus;

public record PaymentConcertResult(
        long paymentAmount,
        ReservationStatus seatStatus,
        QueueStatus queueStatus
) {
}
