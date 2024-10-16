package com.hhplus.concert.core.domain.concert;

public record SelectSeatResult(
        long seatId,
        int position,
        long amount,
        SeatStatus seatStatus
) {
}
