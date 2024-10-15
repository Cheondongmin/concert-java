package com.hhplus.concert.core.interfaces.v1.user.res;

import io.swagger.v3.oas.annotations.media.Schema;

public record SelectUserAmountRes(
        @Schema(description = "남은 금액", defaultValue = "50000")
        int amount
) {
}
