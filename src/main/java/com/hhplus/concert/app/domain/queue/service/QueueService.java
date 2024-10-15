package com.hhplus.concert.app.domain.queue.service;

import com.hhplus.concert.app.domain.queue.entlty.Queue;
import com.hhplus.concert.app.domain.queue.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRepository queueRepository;

    @Transactional
    public String enterQueue(Long userId) {
        Optional<Queue> optUserQueue = queueRepository.findByUserId(userId);
        if (optUserQueue.isPresent()) {
            Queue userQueue = optUserQueue.get();
            if(userQueue.isTokenValid()) {
                return userQueue.getToken();
            }
        }

        String queueToken = UUID.randomUUID().toString();
        queueRepository.save(userId, queueToken);
        return queueToken;
    }
}
