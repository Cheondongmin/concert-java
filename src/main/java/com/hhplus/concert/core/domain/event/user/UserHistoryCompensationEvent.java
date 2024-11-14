package com.hhplus.concert.core.domain.event.user;

public record UserHistoryCompensationEvent(
        UserChargeHistoryInsertEvent historyInsertEvent
) {
}
