package com.hhplus.concert.core.interfaces.api.v1.concert;

import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.interfaces.api.common.CommonRes;
import com.hhplus.concert.core.interfaces.api.v1.concert.req.PaymentConcertReq;
import com.hhplus.concert.core.interfaces.api.v1.concert.req.ReserveConcertReq;
import com.hhplus.concert.core.interfaces.api.v1.concert.res.PaymentConcertRes;
import com.hhplus.concert.core.interfaces.api.v1.concert.res.ReserveConcertRes;
import com.hhplus.concert.core.interfaces.api.v1.concert.res.SelectConcertRes;
import com.hhplus.concert.core.interfaces.api.v1.concert.res.SelectSeatRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "콘서트 API", description = "콘서트 예매와 관련된 API 입니다. 모든 API는 대기열 토큰 헤더(Authorization) 가 필요합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/concerts")
public class ConcertController {

    private final ConcertService concertService;

    @GetMapping("/schedule")
    @Operation(summary = "예약 가능 콘서트 일정 조회")
    public CommonRes<List<SelectConcertRes>> selectConcert(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token
    ) {
        List<SelectConcertResult> concertList = concertService.selectConcertList(token);
        return CommonRes.success(SelectConcertRes.of(concertList));
    }

    @GetMapping("/seat")
    @Operation(summary = "해당 콘서트 일정에 맞는 좌석 조회")
    public CommonRes<List<SelectSeatRes>> selectSeat(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization")  String token,
            @Schema(description = "콘서트 스케쥴 id", defaultValue = "1") @RequestParam("scheduleId") long scheduleId
    ) {
        List<SelectSeatResult> seatList = concertService.selectConcertSeatList(token, scheduleId);
        return CommonRes.success(SelectSeatRes.of(seatList));
    }

    @PostMapping("/reserve")
    @Operation(summary = "해당 콘서트 좌석 임시예약 (5분)")
    public CommonRes<ReserveConcertRes> reserveConcert(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token,
            @RequestBody ReserveConcertReq req
    ) {
        ReserveConcertResult reserveResult = concertService.reserveConcert(token, req.scheduleId(), req.seatId());
        return CommonRes.success(ReserveConcertRes.of(reserveResult));
    }

    @PostMapping("/payment")
    @Operation(summary = "결제 완료 후 임시예약 -> 예약 전환")
    public CommonRes<PaymentConcertRes> paymentConcert(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token,
            @RequestBody PaymentConcertReq req
    ) {
        PaymentConcertResult paymentConcertResult = concertService.paymentConcertWithOptimisticLock(token, req.reservationId());
        return CommonRes.success(PaymentConcertRes.of(paymentConcertResult));
    }
}
