package com.hhplus.concert.fixture.queue;

import com.hhplus.concert.app.domain.queue.entlty.Queue;
import com.hhplus.concert.app.domain.queue.entlty.QueueStatus;

import java.time.LocalDateTime;

public class QueueFixture {

    public static Queue 신규유저_큐(Long userId, QueueStatus queueStatus, LocalDateTime expiredDt) {
        return new Queue(userId, "test-token", queueStatus, expiredDt);
    }
}
