package com.hhplus.concert.core.domain.reservation;

public interface DistributedLock {
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, LockTask<T> task);

    @FunctionalInterface
    interface LockTask<T> {
        T run();
    }
}