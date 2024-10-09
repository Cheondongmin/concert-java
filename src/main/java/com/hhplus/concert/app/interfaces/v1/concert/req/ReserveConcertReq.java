package com.hhplus.concert.app.interfaces.v1.concert.req;

public record ReserveConcertReq(
        long scheduleId,
        long seatId
) {}
