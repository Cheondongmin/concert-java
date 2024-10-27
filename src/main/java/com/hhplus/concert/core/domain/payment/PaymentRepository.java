package com.hhplus.concert.core.domain.payment;

import java.util.List;

public interface PaymentRepository {
    void save(Payment payment);
    Payment findByReservationId(Long id);
    List<Payment> findAll();
}
