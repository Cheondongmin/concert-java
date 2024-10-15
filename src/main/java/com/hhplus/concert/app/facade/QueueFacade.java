package com.hhplus.concert.app.facade;

import com.hhplus.concert.app.domain.queue.service.QueueEnterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueFacade {
    private final QueueEnterService queueEnterService;

    public String enterQueue(Long userId) {
        return queueEnterService.enterQueue(userId);
    }
}
