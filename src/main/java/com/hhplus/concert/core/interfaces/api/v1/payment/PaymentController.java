package com.hhplus.concert.core.interfaces.api.v1.payment;

import com.hhplus.concert.core.domain.payment.PaymentConcertResult;
import com.hhplus.concert.core.domain.payment.PaymentService;
import com.hhplus.concert.core.interfaces.api.common.CommonRes;
import com.hhplus.concert.core.interfaces.api.v1.payment.req.PaymentConcertReq;
import com.hhplus.concert.core.interfaces.api.v1.payment.res.PaymentConcertRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "결제 API", description = "결제와 관련된 API 입니다. 모든 API는 대기열 토큰 헤더(Authorization) 가 필요합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/concerts")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payment")
    @Operation(summary = "결제 완료 후 임시예약 -> 예약 전환")
    public CommonRes<PaymentConcertRes> paymentConcert(
            @Schema(description = "대기열 토큰", defaultValue = "Bearer...") @RequestHeader("Authorization") String token,
            @RequestBody PaymentConcertReq req
    ) {
        PaymentConcertResult paymentConcertResult = paymentService.paymentConcertWithOptimisticLock(token, req.reservationId());
        return CommonRes.success(PaymentConcertRes.of(paymentConcertResult));
    }
}
