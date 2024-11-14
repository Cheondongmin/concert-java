package com.hhplus.concert.core.domain.event.payment;

import com.hhplus.concert.core.domain.payment.PaymentType;

public record PaymentHistoryInsertEvent(
       long userId,
       long amount,
       PaymentType paymentType,
       long paymentId
) {
}