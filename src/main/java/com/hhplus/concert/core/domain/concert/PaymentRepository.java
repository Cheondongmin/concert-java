package com.hhplus.concert.core.domain.concert;

public interface PaymentRepository {
    void save(Payment payment);

    Payment findByReservationId(Long id);
}
