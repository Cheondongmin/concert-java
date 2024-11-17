package com.hhplus.concert.core.infrastructure.event;

import com.hhplus.concert.core.domain.reservation.ReservationMessageSendEvent;
import com.hhplus.concert.core.domain.reservation.ReservationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationEventPublisherImpl implements ReservationEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void reservedMassageSend(ReservationMessageSendEvent reservationMessageSendEvent) {
        applicationEventPublisher.publishEvent(reservationMessageSendEvent);
    }
}
