package com.hhplus.concert.app.interfaces.v1.concert.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record SelectConcertRes(
        @Schema(description = "스케쥴ID", defaultValue = "1")
        long scheduleId,
        @Schema(description = "콘서트 제목", defaultValue = "2024 싸이 흠뻑쇼")
        String concertTitle,
        @Schema(description = "콘서트 개최 날짜", defaultValue = "2024-12-01")
        String openDate,
        @Schema(description = "콘서트 시작 시간", defaultValue = "14:00")
        String startTime,
        @Schema(description = "콘서트 종료 시간", defaultValue = "22:00")
        String endTime,
        @Schema(description = "콘서트 예약 가능 여부(좌석 매진 체크)", defaultValue = "true")
        Boolean seatStatus
) {
}
