package com.hhplus.concert.app.infrastructure.repository.queue.persistence;

import com.hhplus.concert.app.domain.queue.entlty.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueJpaRepository extends JpaRepository<Queue, Long> {
}
