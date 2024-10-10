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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/concerts")
public class ConcertController {

    @GetMapping("/schedule")
    public CommonRes<List<SelectConcertRes>> selectConcert(
            @RequestHeader("Authorization") String token
    ) {
        List<SelectConcertRes> list = new ArrayList<>();
        list.add(new SelectConcertRes(
                1,
                "2024 싸이 흠뻑쇼",
                "2024-12-01",
                "14:00",
                "22:00",
                "AVAILABLE"
        ));
        list.add(new SelectConcertRes(
                2,
                "2024 블랙핑크 콘서트",
                "2024-12-02",
                "14:00",
                "22:00",
                "AVAILABLE"
        ));
        return CommonRes.success(list);
    }

    @GetMapping("/seat")
    public CommonRes<List<SelectSeatRes>> selectSeat(
            @RequestHeader("Authorization") String token,
            @RequestParam("scheduleId") long scheduleId
    ) {
        List<SelectSeatRes> list = new ArrayList<>();
        list.add(new SelectSeatRes(
                1,
                "A1",
                50000,
                "AVAILABLE"
        ));
        list.add(new SelectSeatRes(
                2,
                "A2",
                30000,
                "AVAILABLE"
        ));
        return CommonRes.success(list);
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
