package com.hhplus.concert.core.domain.queue;

import java.time.LocalDateTime;
import java.util.List;

public interface QueueRepository {
    void save(Queue queue);

    Queue findByUserIdForWaitingOrProgress(Long userId);

    List<Queue> findAll();

    List<Queue> findOrderByDescByStatus(QueueStatus queueStatus);

    Queue findByToken(String token);

    Long findStatusIsWaitingAndAlreadyEnteredBy(LocalDateTime enteredDt, QueueStatus queueStatus);

    int countByStatus(QueueStatus queueStatus);

    List<Queue> findTopNWaiting(int remainingSlots);

    void updateQueueToRedis(Queue queue);
}