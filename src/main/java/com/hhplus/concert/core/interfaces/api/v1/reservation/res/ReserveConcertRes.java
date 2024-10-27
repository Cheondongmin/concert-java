package com.hhplus.concert.core.interfaces.api.v1.reservation.res;

import com.hhplus.concert.core.domain.reservation.ReservationStatus;
import com.hhplus.concert.core.domain.reservation.ReserveConcertResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;

public record ReserveConcertRes(
        @Schema(description = "콘서트 예약 Id", defaultValue = "1")
        long reservationId,
        @Schema(description = "콘서트 좌석 상태", defaultValue = "TEMP_RESERVED")
        ReservationStatus seatStatus,
        @Schema(description = "콘서트 임시 예약 날짜", defaultValue = "2024-10-09 20:00:00")
        String reservedDate,
        @Schema(description = "콘서트 임시 예약 만료날짜", defaultValue = "2024-10-09 20:05:00")
        String reservedUntilDate
) {
        private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public static ReserveConcertRes of(ReserveConcertResult reserveResult) {
                return new ReserveConcertRes(
                        reserveResult.reservationId(),
                        reserveResult.seatStatus(),
                        reserveResult.reservedDate().format(dateFormatter),
                        reserveResult.reservedUntilDate().format(dateFormatter)
                );
        }
}
