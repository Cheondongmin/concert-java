package com.hhplus.concert.core.domain.reservation;

import java.time.LocalDateTime;

public record ReservationMessageSendEvent(
        String mail,
        String concertTitle,
        LocalDateTime startDt,
        LocalDateTime confirmDt,
        LocalDateTime untilDt,
        long amount
) {
}
