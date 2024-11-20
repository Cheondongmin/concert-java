package com.hhplus.concert.core.infrastructure.kafka;

import com.hhplus.concert.core.domain.payment.PaymentEventPublisher;
import com.hhplus.concert.core.domain.payment.PaymentMessageSendEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentMessageSendEvent> kafkaTemplate;

    @Override
    public void paymentMassageSend(PaymentMessageSendEvent event) {
        kafkaTemplate.send("payment-notification", event);
    }
}