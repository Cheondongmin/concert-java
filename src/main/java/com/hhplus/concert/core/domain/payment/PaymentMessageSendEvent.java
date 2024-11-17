package com.hhplus.concert.core.domain.payment;

import java.time.LocalDateTime;

public record PaymentMessageSendEvent(
        String mail,
        String concertTitle,
        LocalDateTime startDt,
        LocalDateTime confirmDt,
        long amount
) {
}
