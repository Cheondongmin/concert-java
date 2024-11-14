package com.hhplus.concert.core.domain.payment;

public interface PaymentHistoryRepository {
    void save(PaymentHistory paymentHistory);
    PaymentHistory findByPaymentId(long l);
}
