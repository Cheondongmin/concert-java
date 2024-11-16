package com.hhplus.concert.core.domain.event.user;

import com.hhplus.concert.core.domain.payment.PaymentType;

public record UserChargeHistoryInsertEvent(
       long userId,
       long amount,
       PaymentType paymentType
) {
}