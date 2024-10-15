package com.hhplus.concert.core.interfaces.v1.concert.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReserveConcertRes(
        @Schema(description = "콘서트 예약 Id", defaultValue = "1")
        long reservationId,
        @Schema(description = "콘서트 좌석 상태", defaultValue = "TEMP_RESERVED")
        String seatStatus,
        @Schema(description = "콘서트 임시 예약 날짜", defaultValue = "2024-10-09 20:00:00")
        String reservedDate,
        @Schema(description = "콘서트 임시 예약 만료날짜", defaultValue = "2024-10-09 20:05:00")
        String reservedUntilDate
) {
}
