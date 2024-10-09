package com.hhplus.concert.app.interfaces.v1.concert.res;

public record SelectSeatRes(
        long seatId,
        String position,
        int amount,
        String seatStatus
) {
}
