package com.hhplus.concert.core.domain.reservation;

import com.hhplus.concert.core.domain.concert.ConcertSchedule;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.user.Users;

public record ReservationValidationResult(
        long userId,
        Users user,
        Queue queue,
        ConcertSchedule concertSchedule
) {
}
