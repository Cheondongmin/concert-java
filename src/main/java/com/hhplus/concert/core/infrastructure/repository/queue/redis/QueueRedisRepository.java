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

    /**
     * Queue를 Redis에 추가
     * userId를 key로 하는 Sorted Set에 Queue 객체를 저장하며,
     * score는 현재 시간을 사용하여 시간 순서 보장
     */
    public void add(Queue queue) {
        String key = generateKey(queue.getUserId());
        long currentTime = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, queue, currentTime);
    }

    /**
     * userId로 대기/진행중인 가장 최근 Queue 조회
     */
    public Optional<Queue> findByUserIdForWaitingOrProgress(Long userId) {
        return findLatestQueueByKey(generateKey(userId));
    }

    /**
     * 토큰으로 Queue 조회
     * JWT 토큰에서 userId를 추출하여 해당 유저의 Queue 조회
     */
    public Optional<Queue> findByToken(String token) {
        Long userId = Users.extractUserIdFromJwt(token);
        return findLatestQueueByKey(generateKey(userId));
    }

    /**
     * Redis key 생성
     * "waiting-queue:{userId}" 형태의 key 생성
     */
    private String generateKey(Long userId) {
        return WAITING_QUEUE_KEY + userId;
    }

    /**
     * 특정 key의 Sorted Set에서 가장 최근 Queue 조회
     * range(-1, -1)로 마지막 요소(가장 최근) 조회
     */
    private Optional<Queue> findLatestQueueByKey(String key) {
        Set<Object> waitingQueues = redisTemplate.opsForZSet().range(key, -1, -1);

        if (waitingQueues != null && !waitingQueues.isEmpty()) {
            Object data = waitingQueues.iterator().next();
            Queue queue = objectMapper.convertValue(data, Queue.class);
            return Optional.of(queue);
        }

        return Optional.empty();
    }

    /**
     * 특정 상태의 모든 Queue를 조회하여 시간 역순으로 정렬
     * 1. 모든 waiting-queue: 키를 조회
     * 2. 각 키의 모든 Queue를 가져와서 상태 필터링
     * 3. enteredDt 기준 역순 정렬
     */
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

    /**
     * 특정 시간 이전에 들어온 특정 상태의 Queue 수 조회
     */
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

    /**
     * Queue 상태 업데이트
     * 기존 Queue를 새로운 상태로 업데이트하여 Redis에 저장
     */
    public void updateQueueToRedis(Queue queue) {
        String key = generateKey(queue.getUserId());
        redisTemplate.opsForZSet().add(key, queue, System.currentTimeMillis());
    }

    /**
     * 모든 Queue 조회
     */
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

    /**
     * 특정 상태의 Queue 개수 조회
     */
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

    /**
     * 대기 상태의 Queue 중 상위 N개 조회
     * 1. 모든 waiting-queue: 키를 조회
     * 2. 대기 상태인 Queue만 필터링
     * 3. 시간순 정렬 후 반환
     */
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

    /**
     * 특정 ID 목록의 Queue 상태 일괄 업데이트
     */
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

    /**
     * 모든 Redis 데이터 삭제 (테스트용)
     */
    public void clearAll() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            for (String key : keys) {
                redisTemplate.delete(key);
            }
        }
    }

    /**
     * 만료 조건에 해당하는 Queue 상태 업데이트
     * 특정 상태이면서 특정 시간 이전인 Queue들의 상태를 일괄 업데이트
     */
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
