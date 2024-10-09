package com.hhplus.concert.app.interfaces.v1.concert.res;

public record SelectConcertRes(
        long scheduleId,
        String concertTitle,
        String openDate,
        String startTime,
        String endTime,
        String seatStatus
) {
}
