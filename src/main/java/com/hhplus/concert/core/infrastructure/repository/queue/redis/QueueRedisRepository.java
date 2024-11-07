package com.hhplus.concert.core.infrastructure.repository.queue.redis;

import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String WAITING_QUEUE_KEY = "waiting-queue:";

    // Redis 키 생성
    private String generateKey(Long userId) {
        return WAITING_QUEUE_KEY + userId;
    }

    // Queue를 Redis에 추가
    public void add(Queue queue) {
        String key = generateKey(queue.getUserId());
        long score = queue.getEnteredDt().toEpochSecond(ZoneOffset.UTC);
        String serializedQueue = serialize(queue);
        redisTemplate.opsForZSet().add(key, serializedQueue, score);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);
    }

    // 수동 직렬화 메서드 (객체 -> 문자열)
    private String serialize(Queue queue) {
        return queue.getUserId() + "|" +
                queue.getToken() + "|" +
                queue.getEnteredDt().toEpochSecond(ZoneOffset.UTC) + "|" +
                (queue.getExpiredDt() != null ? queue.getExpiredDt().toEpochSecond(ZoneOffset.UTC) : "") + "|" +
                queue.getStatus().name();
    }

    // 수동 역직렬화 메서드 (문자열 -> 객체)
    private Queue deserialize(String data) {
        String[] parts = data.split("\\|");
        Long userId = Long.parseLong(parts[0]);
        String token = parts[1];
        LocalDateTime enteredDt = LocalDateTime.ofEpochSecond(Long.parseLong(parts[2]), 0, ZoneOffset.UTC);
        LocalDateTime expiredDt = parts[3].isEmpty() ? null : LocalDateTime.ofEpochSecond(Long.parseLong(parts[3]), 0, ZoneOffset.UTC);
        QueueStatus status = QueueStatus.valueOf(parts[4]);

        return new Queue(userId, token, status, enteredDt, expiredDt);
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
        Set<String> queues = redisTemplate.opsForZSet().range(key, -1, -1);
        if (queues != null && !queues.isEmpty()) {
            String data = queues.iterator().next();
            return Optional.of(deserialize(data));
        }
        return Optional.empty();
    }

    // 상태별 Queue를 시간 역순으로 조회
    public List<Queue> findOrderByDescByStatus(QueueStatus status) {
        List<Queue> orderedQueues = new ArrayList<>();
        for (Queue queue : findAll()) {
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
        Set<String> filteredQueues = redisTemplate.opsForZSet().rangeByScore(WAITING_QUEUE_KEY, 0, scoreThreshold);

        long count = 0;
        if (filteredQueues != null) {
            for (String data : filteredQueues) {
                Queue queue = deserialize(data);
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
        String serializedQueue = serialize(queue);
        redisTemplate.opsForZSet().add(key, serializedQueue, score);
    }

    // 모든 Queue 조회
    public List<Queue> findAll() {
        List<Queue> queueList = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null) {
            for (String key : keys) {
                Set<String> queues = redisTemplate.opsForZSet().range(key, 0, -1);
                if (queues != null) {
                    for (String data : queues) {
                        queueList.add(deserialize(data));
                    }
                }
            }
        }
        return queueList;
    }

    // 상태별 Queue 개수 조회
    public int countByStatus(QueueStatus status) {
        int count = 0;
        for (Queue queue : findAll()) {
            if (queue.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    // 대기 상태의 상위 N개 Queue 조회
    public List<Queue> findTopNWaiting(int remainingSlots) {
        List<Queue> waitingQueues = new ArrayList<>();
        for (Queue queue : findAll()) {
            if (queue.getStatus() == QueueStatus.WAITING) {
                waitingQueues.add(queue);
            }
        }
        waitingQueues.sort(Comparator.comparing(Queue::getEnteredDt));
        return waitingQueues.subList(0, Math.min(remainingSlots, waitingQueues.size()));
    }
}
