package com.hhplus.concert.core.domain.queue;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "user-queue")
public class QueueConstant {
    private final Integer maxWaitingNumber;
    private final Integer queueTokenExpireTime;

    public QueueConstant(Integer maxWaitingNumber, Integer queueTokenExpireTime) {
        this.maxWaitingNumber = maxWaitingNumber;
        this.queueTokenExpireTime = queueTokenExpireTime;
    }
}
