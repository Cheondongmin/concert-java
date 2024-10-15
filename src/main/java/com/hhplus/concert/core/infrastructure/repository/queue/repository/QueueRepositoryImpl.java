package com.hhplus.concert.core.infrastructure.repository.queue.repository;

import com.hhplus.concert.core.domain.queue.entlty.Queue;
import com.hhplus.concert.core.domain.queue.entlty.QueueStatus;
import com.hhplus.concert.core.domain.queue.repository.QueueRepository;
import com.hhplus.concert.core.infrastructure.repository.queue.persistence.QueueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QueueRepositoryImpl implements QueueRepository {

    private final QueueJpaRepository queueJpaRepository;

    @Override
    public Queue findByUserId(Long userId) {
        return queueJpaRepository.findById(userId)
                .orElse(null);
    }

    @Override
    public void save(Queue queue) {
        queueJpaRepository.save(queue);
    }

    @Override
    public List<Queue> findAll() {
        return queueJpaRepository.findAll();
    }

    @Override
    public List<Queue> findOrderByDescByStatus(QueueStatus queueStatus) {
        return queueJpaRepository.findAllByStatusOrderByIdDesc(queueStatus);
    }

    @Override
    public Queue findByToken(String token) {
        return queueJpaRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("해당 토큰의 큐가 존재하지 않습니다."));
    }

    @Override
    public Long findStatusIsWaitingAndAlreadyEnteredBy(LocalDateTime enteredDt, QueueStatus queueStatus) {
        return queueJpaRepository.findStatusIsWaitingAndAlreadyEnteredBy(enteredDt, queueStatus);
    }
}
