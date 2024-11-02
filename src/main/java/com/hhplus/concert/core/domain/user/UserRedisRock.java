package com.hhplus.concert.core.domain.user;

public interface UserRedisRock {
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, LockTask<T> task);

    @FunctionalInterface
    interface LockTask<T> {
        T run();
    }
}