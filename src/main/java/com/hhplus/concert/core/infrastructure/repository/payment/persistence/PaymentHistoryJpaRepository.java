package com.hhplus.concert.core.infrastructure.repository.payment.persistence;

import com.hhplus.concert.core.domain.payment.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistory, Long>  {
    PaymentHistory findByPaymentId(long paymentId);
}
