package com.hhplus.concert.core.domain.event.reservation;

public record ReservationCreatedEvent(
       long userId,
       long reservationId,
       long seatId,
       long scheduleId,
       long amount
) {
}
