package com.hhplus.concert.app.interfaces.v1.queue.res;

public record SelectQueueTokenRes(
        long queuePosition,
        String status
) {
}
