package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class CheckQueueIntegrationTest extends IntegrationTest {

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private QueueService queueService;

    @Test
    void 유저가_대기열에서_진입_가능한_상태일_때_큐_상태가_변경된다() {
        // given
        Long userId = 1L;

        Queue queue = new Queue(userId, "test-token", QueueStatus.WAITING, null);
        queueRepository.save(queue);  // DB에 대기열 정보 저장

        // 대기열이 30명 미만인 상황을 시뮬레이션
        List<Queue> waitingQueueList = queueRepository.findOrderByDescByStatus(QueueStatus.WAITING);
        assertThat(waitingQueueList.size()).isLessThan(30); // 30명 미만인 상태 확인

        // when
        SelectQueueTokenResult result = queueService.checkQueue(queue.getToken());

        assertAll(
                () -> assertThat(result.status()).isEqualTo(QueueStatus.PROGRESS),
                () -> assertThat(result.queuePosition()).isEqualTo(0L)
        );
    }

    @Test
    void 유저가_대기열에서_진입_불가능한_상태일_때_큐_상태가_변경되지_않는다() {
        // given
        // 대기열이 30명 이상인 상황을 시뮬레이션
        for (int i = 1; i <= 30; i++) {
            Long userId = (long) i;
            Queue otherQueue = new Queue(userId, "test-token-" + i, QueueStatus.WAITING, null);
            queueRepository.save(otherQueue);
        }

        Long userId = 31L;
        Queue queue = new Queue(userId, "test-token-31", QueueStatus.WAITING, null);
        queueRepository.save(queue);

        // when
        // 대기열이 30명 이상인 상태를 시뮬레이션
        List<Queue> waitingQueueList = queueRepository.findOrderByDescByStatus(QueueStatus.WAITING);
        SelectQueueTokenResult result = queueService.checkQueue(queue.getToken());

        // then
        assertAll(
                () -> assertThat(waitingQueueList.size()).isGreaterThanOrEqualTo(30), // 대기열 30명 확인
                () -> assertThat(result.status()).isEqualTo(QueueStatus.WAITING), // 상태는 여전히 WAITING이어야 함
                () -> assertThat(result.queuePosition()).isEqualTo(31L) // 대기 포지션이 정상적으로 계산되었는지 확인
        );
    }
}
