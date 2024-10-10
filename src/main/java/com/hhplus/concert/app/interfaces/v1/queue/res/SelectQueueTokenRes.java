package com.hhplus.concert.app.interfaces.v1.queue.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record SelectQueueTokenRes(
        @Schema(description = "대기열 순서", defaultValue = "30")
        long queuePosition,
        @Schema(description = "대기열 상태", defaultValue = "WAITING")
        String status
) {}
