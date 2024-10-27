package com.hhplus.concert.core.domain.reservation;

import java.time.LocalDateTime;

public record ReserveConcertResult(
        long reservationId,
        ReservationStatus seatStatus,
        LocalDateTime reservedDate,
        LocalDateTime reservedUntilDate
) {
}
