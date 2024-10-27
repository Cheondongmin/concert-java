package com.hhplus.concert.core.infrastructure.repository.reservation.persistence;

import com.hhplus.concert.core.domain.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
}
