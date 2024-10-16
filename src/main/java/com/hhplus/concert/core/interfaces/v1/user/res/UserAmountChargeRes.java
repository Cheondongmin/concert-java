package com.hhplus.concert.core.interfaces.v1.user.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserAmountChargeRes(
        @Schema(description = "충전 완료 후 남은 금액", defaultValue = "50000")
        Long amount
) {
}
