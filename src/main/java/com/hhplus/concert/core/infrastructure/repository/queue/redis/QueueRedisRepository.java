package com.hhplus.concert.core.infrastructure.repository.queue.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String WAITING_QUEUE_KEY = "waiting-queue:";

    // Redis 키 생성
    private String generateKey(Long userId) {
        return WAITING_QUEUE_KEY + userId;
    }

    // Queue를 Redis에 추가
    public void add(Queue queue) {
        String key = generateKey(queue.getUserId());
        long score = queue.getEnteredDt().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(key, queue, score);
    }

    // userId로 최신 Queue 조회
    public Optional<Queue> findByUserIdForWaitingOrProgress(Long userId) {
        return findLatestQueueByKey(generateKey(userId));
    }

    // 토큰으로 Queue 조회
    public Optional<Queue> findByToken(String token) {
        Long userId = Users.extractUserIdFromJwt(token);
        return findLatestQueueByKey(generateKey(userId));
    }

    // 특정 키의 최신 Queue 조회
    private Optional<Queue> findLatestQueueByKey(String key) {
        Set<Object> queues = redisTemplate.opsForZSet().range(key, -1, -1);
        if (queues != null && !queues.isEmpty()) {
            Object data = queues.iterator().next();
            Queue queue = objectMapper.convertValue(data, Queue.class);
            return Optional.of(queue);
        }
        return Optional.empty();
    }

    // 상태별 Queue를 시간 역순으로 조회
    public List<Queue> findOrderByDescByStatus(QueueStatus status) {
        List<Queue> orderedQueues = new ArrayList<>();
        List<Queue> allQueues = findAll();
        for (Queue queue : allQueues) {
            if (queue.getStatus().equals(status)) {
                orderedQueues.add(queue);
            }
        }
        orderedQueues.sort((q1, q2) -> q2.getEnteredDt().compareTo(q1.getEnteredDt()));
        return orderedQueues;
    }

    // 특정 시간 이전의 상태별 Queue 수 조회
    public long findStatusIsWaitingAndAlreadyEnteredBy(LocalDateTime enteredDt, QueueStatus status) {
        long scoreThreshold = enteredDt.toEpochSecond(ZoneOffset.UTC);
        Set<Object> filteredQueues = redisTemplate.opsForZSet().rangeByScore(WAITING_QUEUE_KEY, 0, scoreThreshold);

        long count = 0;
        if (filteredQueues != null) {
            for (Object data : filteredQueues) {
                Queue queue = objectMapper.convertValue(data, Queue.class);
                if (queue.getStatus() == status && queue.getEnteredDt().isBefore(enteredDt)) {
                    count++;
                }
            }
        }
        return count;
    }

    // Redis에 Queue 상태 업데이트
    public void updateQueueToRedis(Queue queue) {
        String key = generateKey(queue.getUserId());
        long score = queue.getEnteredDt().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add(key, queue, score);
    }

    // 모든 Queue 조회
    public List<Queue> findAll() {
        List<Queue> queueList = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null) {
            for (String key : keys) {
                Set<Object> queues = redisTemplate.opsForZSet().range(key, 0, -1);
                if (queues != null) {
                    for (Object obj : queues) {
                        Queue queue = objectMapper.convertValue(obj, Queue.class);
                        queueList.add(queue);
                    }
                }
            }
        }
        return queueList;
    }

    // 상태별 Queue 개수 조회
    public int countByStatus(QueueStatus status) {
        int count = 0;
        List<Queue> allQueues = findAll();
        for (Queue queue : allQueues) {
            if (queue.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    // 대기 상태의 상위 N개 Queue 조회
    public List<Queue> findTopNWaiting(int remainingSlots) {
        List<Queue> waitingQueues = new ArrayList<>();
        List<Queue> allQueues = findAll();
        for (Queue queue : allQueues) {
            if (queue.getStatus() == QueueStatus.WAITING) {
                waitingQueues.add(queue);
            }
        }
        waitingQueues.sort(Comparator.comparing(Queue::getEnteredDt));
        return waitingQueues.subList(0, Math.min(remainingSlots, waitingQueues.size()));
    }

    // 특정 ID 목록의 Queue 상태 일괄 업데이트
    public void updateStatusByIds(List<Long> ids, QueueStatus newStatus) {
        List<Queue> allQueues = findAll();
        for (Queue queue : allQueues) {
            if (ids.contains(queue.getId())) {
                queue.statusChange(newStatus);
                updateQueueToRedis(queue);
            }
        }
    }

    // 만료 조건에 따라 Queue 상태 업데이트
    public void updateStatusExpire(QueueStatus newStatus, QueueStatus conditionStatus, LocalDateTime conditionExpiredAt) {
        List<Queue> allQueues = findAll();
        for (Queue queue : allQueues) {
            if (queue.getStatus().equals(conditionStatus) && queue.getEnteredDt().isBefore(conditionExpiredAt)) {
                queue.statusChange(newStatus);
                updateQueueToRedis(queue);
            }
        }
    }
}
