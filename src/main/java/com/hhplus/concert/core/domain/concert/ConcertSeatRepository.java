package com.hhplus.concert.core.domain.concert;

import java.util.List;

public interface ConcertSeatRepository {
    List<SelectSeatResult> findConcertSeat(long scheduleId);
    ConcertSeat findByIdWithLock(long seatId);
    ConcertSeat findById(long seatId);
    void save(ConcertSeat concertSeat);
    ConcertSeat findAvailableSeatWithLock(long seatId);
}
