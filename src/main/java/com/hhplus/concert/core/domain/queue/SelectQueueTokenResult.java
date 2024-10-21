package com.hhplus.concert.core.domain.queue;

public record SelectQueueTokenResult(
        long queuePosition,
        QueueStatus status
) {
}
