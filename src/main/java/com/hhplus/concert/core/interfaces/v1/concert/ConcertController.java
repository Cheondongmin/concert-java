package com.hhplus.concert.core.interfaces.v1.concert;

import com.hhplus.concert.core.domain.concert.ConcertService;
import com.hhplus.concert.core.domain.concert.SeatStatus;
import com.hhplus.concert.core.domain.concert.SelectConcertResult;
import com.hhplus.concert.core.interfaces.common.CommonRes;
import com.hhplus.concert.core.interfaces.v1.concert.req.PaymentConcertReq;
import com.hhplus.concert.core.interfaces.v1.concert.req.ReserveConcertReq;
import com.hhplus.concert.core.interfaces.v1.concert.res.PaymentConcertRes;
import com.hhplus.concert.core.interfaces.v1.concert.res.ReserveConcertRes;
import com.hhplus.concert.core.interfaces.v1.concert.res.SelectConcertRes;
import com.hhplus.concert.core.interfaces.v1.concert.res.SelectSeatRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
        List<SelectSeatRes> list = new ArrayList<>();
        list.add(new SelectSeatRes(
                1,
                1,
                50000,
                SeatStatus.AVAILABLE
        ));
        list.add(new SelectSeatRes(
                2,
                2,
                30000,
                SeatStatus.AVAILABLE
        ));
        return CommonRes.success(list);
    }

    @PostMapping("/reserve")
    @Operation(summary = "해당 콘서트 좌석 임시예약 (5분)")
    public CommonRes<ReserveConcertRes> reserveConcert(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token,
            @RequestBody ReserveConcertReq req
    ) {
        return CommonRes.success(
                new ReserveConcertRes(
                        1234,
                        "TEMP_RESERVED",
                        "2024-10-09 20:00:00",
                        "2024-10-09 20:05:00"
                )
        );
    }

    @PostMapping("/payment")
    @Operation(summary = "결제 완료 후 임시예약 -> 예약 전환")
    public CommonRes<PaymentConcertRes> paymentConcert(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token,
            @RequestBody PaymentConcertReq req
    ) {
        return CommonRes.success(
                new PaymentConcertRes(
                        50000,
                        "RESERVED",
                        "DONE"
                )
        );
    }
}
