package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SchedulerQueueServiceTest extends IntegrationTest {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueRepository queueRepository;

    @Test
    public void 대기열진입_테스트() {
        // given: 테스트 준비 (필요한 대기열 상태 생성)
        queueRepository.save(new Queue(1L, "test-token1", QueueStatus.WAITING, null));
        queueRepository.save(new Queue(2L, "test-token2", QueueStatus.WAITING, null));

        // when: 실제로 서비스 메서드를 호출
        queueService.periodicallyEnterUserQueue();

        // then: 결과 검증
        List<Queue> progressedQueues = queueRepository.findAllByStatusOrderByIdDesc(QueueStatus.PROGRESS);
        assertEquals(2, progressedQueues.size()); // 상태가 PROGRESS로 바뀐 대기열이 2개인지 확인
    }
}
