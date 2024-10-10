package com.hhplus.concert.app.interfaces.v1.queue.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateQueueTokenRes(
        @Schema(description = "대기열 토큰", defaultValue = "Bearer...")
        String queueToken
) {
}
