package com.hhplus.concert.core.domain.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService {
    private final QueueRepository queueRepository;

    @Transactional
    public String enterQueue(Long userId) {
        Queue existingQueue = queueRepository.findByUserIdForWaitingOrProgress(userId);

        // 엔티티 체크 (유효성 검증에서 실패시 새로운 객체(토큰) 반환)
        Queue queue = Queue.enterQueue(existingQueue, userId);

        // 변경되지 않은 엔티티는 업데이트 되지 않음.
        queueRepository.save(queue);

        return queue.getToken();
    }

    @Transactional
    public SelectQueueTokenResult checkQueue(String token) {
        long queuePosition = 0L;
        Queue queue = queueRepository.findByToken(token);
        List<Queue> watingQueueList = queueRepository.findOrderByDescByStatus(QueueStatus.WAITING);

        // 큐 체크 후 출입 여부 체크 후 상태변경 된 객체 return (출입 불가능이면 기존 queue return)
        queue.checkWatingQueue(watingQueueList);

        // 만약 상태가 WATING이면, 현재 포지션 가져오기
        if(queue.getStatus().equals(QueueStatus.WAITING)) {
            // 현재 유저의 뒤에 남아있는 대기열 + 1(자기 자신)
            queuePosition = queueRepository.findStatusIsWaitingAndAlreadyEnteredBy(queue.getEnteredDt(), QueueStatus.WAITING) + 1;
        }

        return new SelectQueueTokenResult(queuePosition, queue.getStatus());
    }

    /**
     * 토큰 만료 여부 조건일 경우 토큰 상태값을 업데이트 한다.
     */
    @Transactional
    public void updateExpireConditionToken() {
        queueRepository.updateExpireConditionToken();
    }

    /**
     * 대기열에서 제한 인원에 허용하는 만큼 유저를 진입시킨다
     */
    @Transactional
    public void periodicallyEnterUserQueue() {
        // 현재 진행 중인 대기열의 수를 가져옵니다.
        int currentQueueSize = queueRepository.countByStatus(QueueStatus.PROGRESS);

        // 최대 대기열 수에서 현재 진행 중인 수를 빼서 남은 자리를 계산합니다.
        int maxWaitingNumber = 30;
        int remainingSlots = maxWaitingNumber - currentQueueSize;

        if (remainingSlots > 0) {
            // 남은 자리만큼의 대기 중인 유저를 가져옵니다.
            List<Queue> waitingUserQueues = queueRepository.findTopNWaiting(remainingSlots);

            if (!waitingUserQueues.isEmpty()) {
                // 상태를 PROGRESS로 업데이트합니다.
                queueRepository.updateStatusByIds(
                        waitingUserQueues.stream().map(Queue::getId).collect(Collectors.toList()),
                        QueueStatus.PROGRESS
                );
            }
        }
    }
}
