package com.hhplus.concert.core.infrastructure.repository.queue.repository;

import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.infrastructure.repository.queue.redis.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueueRepositoryImpl implements QueueRepository {

    private final QueueRedisRepository redisRepository;

    @Override
    public Queue findByUserIdForWaitingOrProgress(Long userId) {
        return redisRepository.findByUserIdForWaitingOrProgress(userId)
                .orElse(null);
    }

    @Override
    public void save(Queue queue) {
        redisRepository.add(queue);
    }

    @Override
    public List<Queue> findAll() {
        return redisRepository.findAll();
    }

    @Override
    public List<Queue> findOrderByDescByStatus(QueueStatus queueStatus) {
        return redisRepository.findOrderByDescByStatus(queueStatus);
    }

    @Override
    public Queue findByToken(String token) {
        return redisRepository.findByToken(token)
                .orElseThrow(() -> new NullPointerException("해당 토큰의 큐가 존재하지 않습니다."));
    }

    @Override
    public Long findStatusIsWaitingAndAlreadyEnteredBy(LocalDateTime enteredDt, QueueStatus queueStatus) {
        return redisRepository.findStatusIsWaitingAndAlreadyEnteredBy(enteredDt, queueStatus);
    }

    @Override
    public int countByStatus(QueueStatus queueStatus) {
        return redisRepository.countByStatus(queueStatus);
    }

    @Override
    public List<Queue> findTopNWaiting(int remainingSlots) {
        return redisRepository.findTopNWaiting(remainingSlots);
    }

    @Override
    public void updateStatusByIds(List<Long> collect, QueueStatus queueStatus) {
        redisRepository.updateStatusByIds(collect, queueStatus);
    }

    @Override
    public void updateQueueToRedis(Queue queue) {
        redisRepository.updateQueueToRedis(queue);
    }
}
