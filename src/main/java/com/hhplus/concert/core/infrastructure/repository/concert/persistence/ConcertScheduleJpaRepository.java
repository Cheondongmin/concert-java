package com.hhplus.concert.core.infrastructure.repository.concert.persistence;

import com.hhplus.concert.core.domain.concert.ConcertSchedule;
import com.hhplus.concert.core.domain.concert.SelectConcertResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ConcertScheduleJpaRepository extends JpaRepository<ConcertSchedule, Long> {
    @Query("""
        select new com.hhplus.concert.core.domain.concert.SelectConcertResult(
            cs.id,
            c.title,
            cs.openDt,
            cs.startDt,
            cs.endDt,
            cs.totalSeatStatus
        )
        from
            ConcertSchedule cs join Concert c on cs.concertId = c.id
        where
            cs.startDt >= current_timestamp
        and
            cs.totalSeatStatus = "AVAILABLE"
        and
            cs.isDelete = false
        and
            c.isDelete = false
       """)
    List<SelectConcertResult> findConcertSchedule();

}
