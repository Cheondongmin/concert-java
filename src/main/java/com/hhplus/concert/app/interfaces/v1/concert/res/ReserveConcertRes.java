package com.hhplus.concert.app.interfaces.v1.concert.res;

public record ReserveConcertRes(
        long reservationId,
        String seatStatus,
        String reservedDate,
        String reservedUntilDate
) {
}
