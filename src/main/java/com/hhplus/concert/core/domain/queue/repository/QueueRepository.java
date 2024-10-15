package com.hhplus.concert.core.domain.queue.repository;

import com.hhplus.concert.core.domain.queue.entlty.Queue;
import com.hhplus.concert.core.domain.queue.entlty.QueueStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface QueueRepository {
    void save(Queue queue);
    Queue findByUserId(Long userId);
    List<Queue> findAll();
    List<Queue> findOrderByDescByStatus(QueueStatus queueStatus);
    Queue findByToken(String token);
    Long findStatusIsWaitingAndAlreadyEnteredBy(LocalDateTime enteredDt, QueueStatus queueStatus);
}
