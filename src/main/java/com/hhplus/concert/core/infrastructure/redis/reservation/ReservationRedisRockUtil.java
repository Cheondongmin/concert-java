package com.hhplus.concert.core.infrastructure.redis.reservation;

import com.hhplus.concert.core.domain.reservation.ReservationRedisRock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ReservationRedisRockUtil implements ReservationRedisRock {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, LockTask<T> task) {
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // waitTime 동안 락 획득을 시도
            boolean isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Failed to acquire lock for key: " + lockKey);
            }

            // 락 획득 성공, task 실행
            return task.run();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted", e);
        } finally {
            // 현재 스레드가 락을 보유하고 있는 경우에만 unlock
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                // unlock 실패 로깅
            }
        }
    }
}
