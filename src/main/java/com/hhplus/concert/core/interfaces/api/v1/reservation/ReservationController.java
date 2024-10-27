package com.hhplus.concert.core.interfaces.api.v1.reservation;

import com.hhplus.concert.core.domain.reservation.ReservationService;
import com.hhplus.concert.core.domain.reservation.ReserveConcertResult;
import com.hhplus.concert.core.interfaces.api.common.CommonRes;
import com.hhplus.concert.core.interfaces.api.v1.reservation.req.ReserveConcertReq;
import com.hhplus.concert.core.interfaces.api.v1.reservation.res.ReserveConcertRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "콘서트 API", description = "콘서트 예매와 관련된 API 입니다. 모든 API는 대기열 토큰 헤더(Authorization) 가 필요합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/concerts")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reserve")
    @Operation(summary = "해당 콘서트 좌석 임시예약 (5분)")
    public CommonRes<ReserveConcertRes> reserveConcert(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token,
            @RequestBody ReserveConcertReq req
    ) {
        ReserveConcertResult reserveResult = reservationService.reserveConcert(token, req.scheduleId(), req.seatId());
        return CommonRes.success(ReserveConcertRes.of(reserveResult));
    }
}
