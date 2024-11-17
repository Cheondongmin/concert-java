package com.hhplus.concert.core.infrastructure.event;

import com.hhplus.concert.core.domain.payment.PaymentEventPublisher;
import com.hhplus.concert.core.domain.payment.PaymentMessageSendEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void paymentMassageSend(PaymentMessageSendEvent reservationMessageSendEvent) {
        applicationEventPublisher.publishEvent(reservationMessageSendEvent);
    }
}
