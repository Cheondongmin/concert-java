package com.hhplus.concert.core.infrastructure.repository.payment.repository;

import com.hhplus.concert.core.domain.payment.PaymentHistory;
import com.hhplus.concert.core.domain.payment.PaymentHistoryRepository;
import com.hhplus.concert.core.infrastructure.repository.payment.persistence.PaymentHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentHistoryRepositoryImpl implements PaymentHistoryRepository {
    private final PaymentHistoryJpaRepository jpaRepository;

    @Override
    public void save(PaymentHistory paymentHistory) {
        jpaRepository.save(paymentHistory);
    }
}
