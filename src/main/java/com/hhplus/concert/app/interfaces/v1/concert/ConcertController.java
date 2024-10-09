package com.hhplus.concert.app.interfaces.v1.concert;

import com.hhplus.concert.app.interfaces.common.CommonRes;
import com.hhplus.concert.app.interfaces.v1.concert.req.PaymentConcertReq;
import com.hhplus.concert.app.interfaces.v1.concert.req.ReserveConcertReq;
import com.hhplus.concert.app.interfaces.v1.concert.res.PaymentConcertRes;
import com.hhplus.concert.app.interfaces.v1.concert.res.ReserveConcertRes;
import com.hhplus.concert.app.interfaces.v1.concert.res.SelectConcertRes;
import com.hhplus.concert.app.interfaces.v1.concert.res.SelectSeatRes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/concerts")
public class ConcertController {

    @GetMapping("/schedule")
    public CommonRes<SelectConcertRes> selectConcert(
            @RequestHeader("Authorization") String token
    ) {
        return CommonRes.success(
                new SelectConcertRes(
                        1,
                        "2024 싸이 흠뻑쇼",
                        "2024-12-01",
                        "14:00",
                        "22:00",
                        "AVAILABLE"
                )
        );
    }

    @GetMapping("/seat")
    public CommonRes<SelectSeatRes> selectSeat(
            @RequestHeader("Authorization") String token,
            @RequestParam("scheduleId") long scheduleId
    ) {
        return CommonRes.success(
                new SelectSeatRes(
                        100,
                        "A1",
                        50000,
                        "AVAILABLE"
                )
        );
    }

    @PostMapping("/reserve")
    public CommonRes<ReserveConcertRes> reserveConcert(
            @RequestHeader("Authorization") String token,
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
    public CommonRes<PaymentConcertRes> paymentConcert(
            @RequestHeader("Authorization") String token,
            @RequestBody PaymentConcertReq req
    ) {
        return CommonRes.success(
                new PaymentConcertRes(
                        "콘서트 예매가 성공적으로 완료되었습니다.",
                        50000,
                        "RESERVED",
                        "DONE"
                )
        );
    }
}
