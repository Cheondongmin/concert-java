package com.hhplus.concert.core.domain.queue.dto;

import com.hhplus.concert.core.domain.queue.entlty.QueueStatus;

public record SelectQueueTokenResult(
        long queuePosition,
        QueueStatus status
) {
}
