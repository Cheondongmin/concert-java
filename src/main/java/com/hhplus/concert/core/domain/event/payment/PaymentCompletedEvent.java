package com.hhplus.concert.core.domain.event.payment;
import com.hhplus.concert.core.domain.payment.PaymentType;

public record PaymentCompletedEvent(
        long userId,
        long paymentId,
        long amount,
        PaymentType type
) {
}
