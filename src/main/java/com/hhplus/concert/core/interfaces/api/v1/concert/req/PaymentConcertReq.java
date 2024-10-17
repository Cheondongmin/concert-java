package com.hhplus.concert.core.interfaces.api.v1.concert.req;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentConcertReq(
        @Schema(description = "콘서트 예약 id", defaultValue = "1")
        long reservationId
) {}
