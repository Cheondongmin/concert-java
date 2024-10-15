package com.hhplus.concert.app.infrastructure.repository.queue.repository;

import com.hhplus.concert.app.domain.queue.entlty.Queue;
import com.hhplus.concert.app.domain.queue.repository.QueueRepository;
import com.hhplus.concert.app.infrastructure.repository.queue.persistence.QueueJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QueueRepositoryImpl implements QueueRepository {

    private final QueueJpaRepository queueJpaRepository;

    @Override
    public Optional<Queue> findByUserId(Long userId) {
        return queueJpaRepository.findById(userId);
    }

    @Override
    public void save(Queue queue) {
        queueJpaRepository.save(queue);
    }

    @Override
    public List<Queue> findAll() {
        return queueJpaRepository.findAll();
    }
}
