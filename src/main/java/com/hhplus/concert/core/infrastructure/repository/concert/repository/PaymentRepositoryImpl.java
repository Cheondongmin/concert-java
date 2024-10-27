package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.payment.Payment;
import com.hhplus.concert.core.domain.payment.PaymentRepository;
import com.hhplus.concert.core.infrastructure.repository.concert.persistence.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository jpaRepository;

    @Override
    public void save(Payment payment) {
        jpaRepository.save(payment);
    }

    @Override
    public Payment findByReservationId(Long id) {
        return jpaRepository.findByReservationId(id).orElseThrow(
                () -> new NullPointerException("해당 예약 아이디의 결제정보가 존재하지 않습니다.")
        );
    }

    @Override
    public List<Payment> findAll() {
        return jpaRepository.findAll();
    }
}
