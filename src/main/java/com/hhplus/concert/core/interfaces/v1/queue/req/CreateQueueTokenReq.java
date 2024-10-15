package com.hhplus.concert.core.interfaces.v1.queue.req;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateQueueTokenReq(
        @Schema(description = "유저 id", defaultValue = "1")
        Long userId
) {
}
