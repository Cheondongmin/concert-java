package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.infrastructure.repository.concert.persistence.ConcertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository jpaRepository;

    @Override
    public Concert findById(Long concertId) {
        return jpaRepository.findById(concertId).orElseThrow(
                () -> new NullPointerException("해당 아이디를 가진 콘서트가 존재하지 않습니다."));
    }

    @Override
    public void save(Concert concert) {
        jpaRepository.save(concert);
    }

    @Override
    public List<Reservation> findReservationReleaseTarget(LocalDateTime expiredAt, ReservationStatus status, PaymentStatus paymentStatus) {
        return jpaRepository.findReservationReleaseTarget(expiredAt, status, paymentStatus);
    }

    @Override
    public void deleteReservation(List<Reservation> reservations) {
        jpaRepository.deleteAllInBatch();
    }

    @Override
    public void deletePaymentBy(List<Long> reservationIds) {
        jpaRepository.deleteAllBy(reservationIds);
    }

    @Override
    public void deleteSeats(List<Long> seatIds) {
        jpaRepository.deleteAllById(seatIds);
    }
}
