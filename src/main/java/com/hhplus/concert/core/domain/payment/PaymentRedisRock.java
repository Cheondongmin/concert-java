package com.hhplus.concert.core.domain.payment;

public interface PaymentRedisRock {
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, LockTask<T> task);

    @FunctionalInterface
    interface LockTask<T> {
        T run();
    }
}