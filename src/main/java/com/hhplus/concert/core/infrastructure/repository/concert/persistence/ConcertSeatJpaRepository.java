package com.hhplus.concert.core.infrastructure.repository.concert.persistence;

import com.hhplus.concert.core.domain.concert.ConcertSeat;
import com.hhplus.concert.core.domain.concert.SelectSeatResult;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConcertSeatJpaRepository extends JpaRepository<ConcertSeat, Long> {
    @Query("""
        select new com.hhplus.concert.core.domain.concert.SelectSeatResult(
            cs.id,
            cs.position,
            cs.amount,
            cs.seatStatus
        )
        from
            ConcertSeat cs join ConcertSchedule csh on cs.concertScheduleId = csh.id
        where
            csh.id = :scheduleId
        and
            cs.isDelete = false
        and
            csh.isDelete = false
        and
            cs.seatStatus = "AVAILABLE"
       """)
    List<SelectSeatResult> findConcertSeat(
            @Param("scheduleId") long scheduleId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cs FROM ConcertSeat cs WHERE cs.id = :seatId")
    Optional<ConcertSeat> findByIdWithLock(
            @Param("seatId") Long seatId
    );
}
