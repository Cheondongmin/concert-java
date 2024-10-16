package com.hhplus.concert.core.interfaces.v1.user.req;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserAmountChargeReq(
        @Schema(description = "충전금액", defaultValue = "50000")
        Long amount
) {
}
