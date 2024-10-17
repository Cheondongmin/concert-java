package com.hhplus.concert.core.domain.concert;

public interface ReservationRepository {
    void save(Reservation reservation);

    Reservation findById(long reservationId);
}
