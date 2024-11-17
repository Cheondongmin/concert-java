package com.hhplus.concert.core.domain.reservation;

import org.springframework.stereotype.Component;

@Component
public interface ReservationEventPublisher {
    void reservedMassageSend(ReservationMessageSendEvent reservationMessageSendEvent);
}
