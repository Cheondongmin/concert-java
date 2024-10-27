package com.hhplus.concert.core.domain.payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentServiceUnitTest {

    @Test
    void 결제를_진행한다() {
        // given
        long userId = 1L; // 유저 ID 직접 사용

        // when
        Payment payment = Payment.enterPayment(userId, 1L, 500, PaymentStatus.PROGRESS);

        // 결제 완료 처리
        payment.finishPayment();

        // then
        assertEquals(PaymentStatus.DONE, payment.getStatus());
        assertEquals(500L, payment.getPrice()); // 결제 후 금액 확인
    }
}
