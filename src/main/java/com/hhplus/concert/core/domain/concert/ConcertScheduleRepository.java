package com.hhplus.concert.core.domain.concert;

import java.util.List;

public interface ConcertScheduleRepository {
    List<SelectConcertResult> findConcertSchedule();
    ConcertSchedule findById(long scheduleId);
    void save(ConcertSchedule concertSchedule);
}
