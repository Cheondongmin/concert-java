package com.hhplus.concert.core.infrastructure.repository.concert.persistence;

import com.hhplus.concert.core.domain.concert.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHistoryJpaRepository extends JpaRepository<PaymentHistory, Long>  {
}
