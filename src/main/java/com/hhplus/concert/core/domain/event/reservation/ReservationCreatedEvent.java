package com.hhplus.concert.core.domain.event.reservation;

import java.time.LocalDateTime;

public record ReservationCreatedEvent(
       long userId,
       long reservationId,
       long seatId,
       long scheduleId,
       long amount,
       LocalDateTime startDt
) {
}
