package com.hhplus.concert.app.domain.queue.service;

import com.hhplus.concert.app.domain.queue.entlty.Queue;
import com.hhplus.concert.app.domain.queue.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueueEnterService {

    private final QueueRepository queueRepository;

    @Transactional
    public String enterQueue(Long userId) {
        Queue existingQueue = queueRepository.findByUserId(userId).orElse(null);

        // 엔티티 체크 (유효성 검증에서 실패시 새로운 객체(토큰) 반환)
        Queue queue = Queue.enterQueue(existingQueue, userId);

        // 변경되지 않은 엔티티는 업데이트 되지 않음.
        queueRepository.save(queue);

        return queue.getToken();
    }
}
