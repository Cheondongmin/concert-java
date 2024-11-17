package com.hhplus.concert.core.domain.payment;

import org.springframework.stereotype.Component;

@Component
public interface PaymentEventPublisher {
    void paymentMassageSend(PaymentMessageSendEvent reservationMessageSendEvent);
}
