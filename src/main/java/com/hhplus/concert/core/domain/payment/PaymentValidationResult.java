package com.hhplus.concert.core.domain.payment;

import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.reservation.Reservation;
import com.hhplus.concert.core.domain.user.Users;

public record PaymentValidationResult(
        Long userId,
        Users user,
        Queue queue,
        Reservation reservation
) {
}
