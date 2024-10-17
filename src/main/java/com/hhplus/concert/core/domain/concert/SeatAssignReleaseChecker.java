package com.hhplus.concert.core.domain.concert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional
@RequiredArgsConstructor
public class SeatAssignReleaseChecker {

    private final ConcertRepository concertRepository;

    public void release() {
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(5);
        List<Reservation> reservations = concertRepository.findReservationReleaseTarget(expiredAt, ReservationStatus.TEMP_RESERVED, PaymentStatus.DONE);
        concertRepository.deleteReservation(reservations);
        concertRepository.deletePaymentBy(getReservationIds(reservations));
        concertRepository.deleteSeats(getSeatIds(reservations));
    }


    private List<Long> getReservationIds(List<Reservation> reservations) {
        return reservations.stream().map(Reservation::getId).toList();
    }

    private List<Long> getSeatIds(List<Reservation> reservations) {
        return reservations.stream().map(Reservation::getSeatId).toList();
    }

}
