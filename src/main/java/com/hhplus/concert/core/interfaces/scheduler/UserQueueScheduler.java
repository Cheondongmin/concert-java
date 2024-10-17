package com.hhplus.concert.core.interfaces.scheduler;

import com.hhplus.concert.core.domain.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserQueueScheduler {

    private final QueueService userQueueService;

    @Scheduled(fixedDelay = 5000, initialDelay = 1800_000)
    public void checkTokenExpire() {
        userQueueService.updateExpireConditionToken();
    }

    @Scheduled(fixedDelay = 5000)
    public void enteringUserQueue() {
        userQueueService.periodicallyEnterUserQueue();
    }
}
