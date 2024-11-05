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
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String WAITING_QUEUE_KEY = "waiting-queue:";

    public void add(Queue queue) {
        String key = generateKey(queue.getUserId());
        long currentTime = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, queue, currentTime);
    }

    public Optional<Queue> findByUserIdForWaitingOrProgress(Long userId) {
        return findLatestQueueByKey(generateKey(userId));
    }

    public Optional<Queue> findByToken(String token) {
        Long userId = Users.extractUserIdFromJwt(token);
        return findLatestQueueByKey(generateKey(userId));
    }

    private String generateKey(Long userId) {
        return WAITING_QUEUE_KEY + userId;
    }

    private Optional<Queue> findLatestQueueByKey(String key) {
        Set<Object> waitingQueues = redisTemplate.opsForZSet().range(key, -1, -1);

        if (waitingQueues != null && !waitingQueues.isEmpty()) {
            Object data = waitingQueues.iterator().next();
            Queue queue = objectMapper.convertValue(data, Queue.class);
            return Optional.of(queue);
        }

        return Optional.empty();
    }

    public List<Queue> findOrderByDescByStatus(QueueStatus status) {
        List<Queue> filteredQueues = new ArrayList<>();

        // 모든 `waiting-queue:` 키 조회
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                // 각 키에 대해 ZSet에서 모든 Queue 항목을 조회
                Set<Object> queues = redisTemplate.opsForZSet().range(key, 0, -1);
                if (queues != null) {
                    for (Object data : queues) {
                        Queue queue = objectMapper.convertValue(data, Queue.class);
                        // 상태가 일치하는 항목만 필터링
                        if (queue.getStatus().equals(status)) {
                            filteredQueues.add(queue);
                        }
                    }
                }
            }
        }

        // enteredDt 기준으로 내림차순 정렬
        filteredQueues.sort(Comparator.comparing(Queue::getEnteredDt).reversed());

        return filteredQueues;
    }

    public long findStatusIsWaitingAndAlreadyEnteredBy(LocalDateTime enteredDt, QueueStatus status) {
        Set<Object> allWaitingQueues = redisTemplate.opsForZSet().rangeByScore(WAITING_QUEUE_KEY, 0, Double.MAX_VALUE);

        long count = 0;
        if (allWaitingQueues != null) {
            for (Object data : allWaitingQueues) {
                Queue queue = objectMapper.convertValue(data, Queue.class);
                if (queue.getStatus() == status && queue.getEnteredDt().isBefore(enteredDt)) {
                    count++;
                }
            }
        }

        return count;
    }

    public void updateQueueToRedis(Queue queue) {
        String key = generateKey(queue.getUserId());
        redisTemplate.opsForZSet().add(key, queue, System.currentTimeMillis());
    }

    public List<Queue> findAll() {
        List<Queue> queueList = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null && !keys.isEmpty()) {
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

    public int countByStatus(QueueStatus status) {
        int count = 0;
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null) {
            for (String key : keys) {
                Set<Object> queues = redisTemplate.opsForZSet().range(key, 0, -1);
                if (queues != null) {
                    count += (int) queues.stream()
                            .map(data -> objectMapper.convertValue(data, Queue.class))
                            .filter(queue -> queue.getStatus().equals(status))
                            .count();
                }
            }
        }
        return count;
    }

    public List<Queue> findTopNWaiting(int remainingSlots) {
        List<Queue> waitingQueues = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null) {
            for (String key : keys) {
                Set<Object> queues = redisTemplate.opsForZSet().range(key, 0, -1);
                if (queues != null) {
                    queues.stream()
                            .map(data -> objectMapper.convertValue(data, Queue.class))
                            .filter(queue -> queue.getStatus().equals(QueueStatus.WAITING))
                            .forEach(waitingQueues::add);
                }
            }
        }

        // enteredDt 기준으로 정렬 후 remainingSlots만큼 제한
        return waitingQueues.stream()
                .sorted(Comparator.comparing(Queue::getEnteredDt))
                .limit(remainingSlots)
                .collect(Collectors.toList());
    }

    public void updateStatusByIds(List<Long> ids, QueueStatus status) {
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null) {
            for (String key : keys) {
                Set<Object> queues = redisTemplate.opsForZSet().range(key, 0, -1);
                if (queues != null) {
                    queues.forEach(data -> {
                        Queue queue = objectMapper.convertValue(data, Queue.class);
                        if (ids.contains(queue.getId())) {
                            // 상태 업데이트 후 Redis에 재삽입
                            queue.statusChange(status);
                            // ZoneOffset을 지정하여 epoch 시간으로 변환
                            long score = queue.getEnteredDt().toEpochSecond(ZoneOffset.UTC);
                            redisTemplate.opsForZSet().add(key, queue, score);
                        }
                    });
                }
            }
        }
    }

    public void clearAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            for (String key : keys) {
                redisTemplate.delete(key);
            }
        }
    }

    public void updateStatusExpire(QueueStatus updateStatus, QueueStatus conditionStatus, LocalDateTime conditionExpiredAt) {
        Set<String> keys = redisTemplate.keys(WAITING_QUEUE_KEY + "*");

        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                Set<Object> queues = redisTemplate.opsForZSet().range(key, 0, -1);

                if (queues != null) {
                    for (Object data : queues) {
                        Queue queue = objectMapper.convertValue(data, Queue.class);

                        // 조건에 맞는 항목만 상태 업데이트
                        if (queue.getStatus().equals(conditionStatus) && queue.getEnteredDt().isBefore(conditionExpiredAt)) {
                            queue.statusChange(updateStatus);

                            // 업데이트 후 Redis에 재삽입
                            redisTemplate.opsForZSet().add(key, queue, queue.getEnteredDt().toEpochSecond(ZoneOffset.UTC));
                        }
                    }
                }
            }
        }
    }
}
