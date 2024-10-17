package com.hhplus.concert.core.domain.concert;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertRepository {
    Concert findById(Long concertId);
    void save(Concert concert);
    List<Reservation> findReservationReleaseTarget(LocalDateTime expiredAt,  ReservationStatus status, PaymentStatus paymentStatus);
    void deleteReservation(List<Reservation> reservations);
    void deletePaymentBy(List<Long> reservationIds);
    void deleteSeats(List<Long> seatIds);
}
