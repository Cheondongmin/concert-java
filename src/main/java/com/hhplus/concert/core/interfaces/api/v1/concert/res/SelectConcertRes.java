package com.hhplus.concert.core.interfaces.api.v1.concert.res;

import com.hhplus.concert.core.domain.concert.SelectConcertResult;
import com.hhplus.concert.core.domain.concert.TotalSeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
        TotalSeatStatus seatStatus
) {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public static List<SelectConcertRes> of(List<SelectConcertResult> resultList) {
        List<SelectConcertRes> resList = new ArrayList<>();
        for (SelectConcertResult result : resultList) {
            resList.add(new SelectConcertRes(
                    result.scheduleId(),
                    result.concertTitle(),
                    result.openDate().format(dateFormatter),
                    result.startTime().format(dateTimeFormatter),
                    result.endTime().format(dateTimeFormatter),
                    result.seatStatus()
            ));
        }
        return resList;
    }
}
