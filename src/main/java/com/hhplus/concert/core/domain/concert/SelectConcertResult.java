package com.hhplus.concert.core.domain.concert;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SelectConcertResult(
        long scheduleId,
        String concertTitle,
        LocalDate openDate,
        LocalDateTime startTime,
        LocalDateTime endTime,
        TotalSeatStatus seatStatus
) {
}
