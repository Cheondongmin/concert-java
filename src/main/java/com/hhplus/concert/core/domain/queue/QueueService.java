package com.hhplus.concert.core.domain.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
