package com.hhplus.concert.core.domain.event.user;

public record UserBalanceChangedEvent (
        long userId,
        long amount,
        String changeType
){
}
