package com.hhplus.concert.core.domain.concert;

import java.time.LocalDateTime;

public record ReserveConcertResult(
        long reservationId,
        ReservationStatus seatStatus,
        LocalDateTime reservedDate,
        LocalDateTime reservedUntilDate
) {
}
