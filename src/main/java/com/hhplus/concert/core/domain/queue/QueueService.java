package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.exception.ExceptionCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
    private final QueueRepository queueRepository;

    @CircuitBreaker(name = "queueService", fallbackMethod = "enterQueueFallback")
    @Transactional
    public String enterQueue(Long userId) {
        try {
            Queue existingQueue = queueRepository.findByUserIdForWaitingOrProgress(userId);

            // 엔티티 체크 (유효성 검증에서 실패시 새로운 객체(토큰) 반환)
            Queue queue = Queue.enterQueue(existingQueue, userId);

            queueRepository.save(queue);

            return queue.getToken();
        } catch (Exception e) {
            log.error("Error in enterQueue: {}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private String enterQueueFallback(Long userId, Exception e) {
        log.error("Fallback triggered for enterQueue. userId: {}, error: {}", userId, e.getMessage(), e);
        if (e instanceof ApiException) {
            throw (ApiException) e;
        }
        throw new ApiException(ExceptionCode.E500, LogLevel.ERROR);
    }

    @CircuitBreaker(name = "queueService", fallbackMethod = "checkQueueFallback")
    @Transactional
    public SelectQueueTokenResult checkQueue(String token) {
        try {
            long queuePosition = 0L;
            Queue queue = queueRepository.findByToken(token);
            List<Queue> watingQueueList = queueRepository.findOrderByDescByStatus(QueueStatus.WAITING);

            // 큐 체크 후 출입 여부 체크 후 상태변경 된 객체 return (출입 불가능이면 기존 queue return)
            queue.checkWaitingQueue(watingQueueList);

            // 만약 상태가 WATING이면, 현재 포지션 가져오기
            if(queue.getStatus().equals(QueueStatus.WAITING)) {
                // 현재 유저의 뒤에 남아있는 대기열 + 1(자기 자신)
                queuePosition = queueRepository.findStatusIsWaitingAndAlreadyEnteredBy(queue.getEnteredDt(), QueueStatus.WAITING) + 1;
            }

            queueRepository.updateQueueToRedis(queue);

            return new SelectQueueTokenResult(queuePosition, queue.getStatus());
        } catch (Exception e) {
            log.error("Error in checkQueue: {}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private SelectQueueTokenResult checkQueueFallback(String token, Exception e) {
        if (e instanceof ApiException) {
            throw (ApiException) e;
        }
        log.error("Circuit breaker - checkQueue failed. token: {}, error: {}", token, e.getMessage());
        throw new ApiException(ExceptionCode.E500, LogLevel.ERROR);
    }

    @CircuitBreaker(name = "queueService", fallbackMethod = "periodicallyEnterUserQueueFallback")
    @Transactional
    public void periodicallyEnterUserQueue() {
        try {
            int currentQueueSize = queueRepository.countByStatus(QueueStatus.PROGRESS);
            int maxWaitingNumber = 30;
            int remainingSlots = maxWaitingNumber - currentQueueSize;

            if (remainingSlots > 0) {
                List<Queue> waitingUserQueues = queueRepository.findTopNWaiting(remainingSlots);
                if (!waitingUserQueues.isEmpty()) {
                    waitingUserQueues.forEach(queue -> {
                        queue.statusChange(QueueStatus.PROGRESS);
                        queueRepository.save(queue);
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error in periodicallyEnterUserQueue: {}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private void periodicallyEnterUserQueueFallback(Exception e) {
        if (e instanceof ApiException) {
            throw (ApiException) e;
        }
        log.error("Circuit breaker - periodicallyEnterUserQueue failed. error: {}", e.getMessage());
        // 주기적 업데이트는 다음 스케줄에서 재시도
    }
}
