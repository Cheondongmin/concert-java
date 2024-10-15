package com.hhplus.concert.core.infrastructure.repository.queue.persistence;

import com.hhplus.concert.core.domain.queue.entlty.Queue;
import com.hhplus.concert.core.domain.queue.entlty.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueJpaRepository extends JpaRepository<Queue, Long> {
    List<Queue> findAllByStatusOrderByIdDesc(QueueStatus queueStatus);
    Optional<Queue> findByToken(String token);
    Long countByEnteredDtAndStatus(LocalDateTime enteredDt, QueueStatus status);
}
