package com.hhplus.concert.core.infrastructure.repository.concert.persistence;

import com.hhplus.concert.core.domain.concert.Concert;
import com.hhplus.concert.core.domain.payment.PaymentStatus;
import com.hhplus.concert.core.domain.reservation.Reservation;
import com.hhplus.concert.core.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {

    @Query("""
        select r from Reservation r
        left join Payment p on r.id = p.reservationId
        where r.reservedDt < :expiredDt
        and r.status = :status
        and (p.id is null or p.status <> :paymentStatus)
        group by r.id
        """)
    List<Reservation> findReservationReleaseTarget(@Param("expiredDt") LocalDateTime expiredAt,
                                                   @Param("status") ReservationStatus status,
                                                   @Param("paymentStatus") PaymentStatus paymentStatus);

    @Modifying
    @Query("delete from Payment p where p.reservationId in :reservationIds")
    void deleteAllBy(@Param("reservationIds") List<Long> reservationIds);
}
