package com.hhplus.concert.core.domain.event.payment;

public record PaymentHistoryCompensationEvent(
        PaymentHistoryInsertEvent historyInsertEvent
) {
}
