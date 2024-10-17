package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.IntegrationTest;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class EnterQueueIntegrationTest extends IntegrationTest {

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private QueueService queueService;

    @Test
    void 대기열에_존재하지_않을경우_유저를_등록하고_대기열_토큰을_반환한다() {
        // given
        Long userId = 1L;

        // when
        String queueToken = queueService.enterQueue(userId);

        // then
        List<Queue> userQueues = queueRepository.findAll();
        assertAll(
                () -> assertThat(queueToken).isEqualTo(userQueues.get(0).getToken()), // 토큰 확인
                () -> AssertionsForInterfaceTypes.assertThat(userQueues).hasSize(1)   // 대기열에 유저 1명 확인
        );
    }

    @Test
    void 대기열에_존재할_경우_기존_대기열_토큰을_반환한다() {
        // given
        String existQueueToken = "existQueueToken";
        Long userId = 1L;

        queueRepository.save(new Queue(userId, existQueueToken)); // 기존 유저 저장

        // when
        String queueToken = queueService.enterQueue(userId); // 이미 대기 중인 유저가 다시 대기열에 들어가려는 상황

        // then
        List<Queue> userQueues = queueRepository.findAll();
        assertAll(
                () -> assertThat(existQueueToken).isEqualTo(queueToken),             // 기존 토큰을 반환해야 함
                () -> AssertionsForInterfaceTypes.assertThat(userQueues).hasSize(1)  // 대기열 인원이 1명이어야 함
        );
    }

    @Test
    void 제한인원이_30명인_큐에서_PROGRESS_상태인_대기열이_30명_미만일_경우_상태값과_만료시간을_업데이트_후_대기순번_0을_반환한다() {
        // given
        // 대기열에 29명 추가
        for (int i = 1; i <= 29; i++) {
            Queue otherQueue = new Queue((long) i, "test-token-" + i, QueueStatus.WAITING, LocalDateTime.now().plusMinutes(5));
            queueRepository.save(otherQueue);
        }

        // 테스트할 유저의 대기열을 추가
        Queue testQueue = new Queue(30L, "test-token-30", QueueStatus.WAITING, LocalDateTime.now().plusMinutes(5));
        queueRepository.save(testQueue);

        // when
        SelectQueueTokenResult result = queueService.checkQueue(testQueue.getToken());

        // then
        Queue updatedQueue = queueRepository.findByToken(testQueue.getToken());
        assertAll(
                () -> assertThat(updatedQueue.getStatus()).isEqualTo(QueueStatus.PROGRESS),    // 상태가 PROGRESS로 변경되었는지 확인
                () -> assertThat(updatedQueue.getExpiredDt()).isAfter(LocalDateTime.now()),    // 만료 시간이 연장되었는지 확인
                () -> assertThat(result.queuePosition()).isEqualTo(0L)                        // 대기 순번이 0인지 확인
        );
    }

    @Test
    void 제한인원이_30명인_큐에서_PROGRESS_상태인_대기열이_5명_미만일_경우_상태값과_만료시간을_업데이트_후_대기순번_0을_반환한다() {
        // given: 5명이 진행 중(PROGRESS)이고 추가로 대기 중(WAITING)인 유저가 있을 경우
        LocalDateTime now = LocalDateTime.now();
        List<Queue> queueList =
                List.of(
                        new Queue(1L, "token-1", QueueStatus.PROGRESS, now.plusMinutes(10)),
                        new Queue(2L, "token-2", QueueStatus.PROGRESS, now.plusMinutes(9)),
                        new Queue(3L, "token-3", QueueStatus.PROGRESS, now.plusMinutes(8)),
                        new Queue(4L, "token-4", QueueStatus.PROGRESS, now.plusMinutes(7)),
                        new Queue(5L, "token-5", QueueStatus.PROGRESS, now.plusMinutes(6)),
                        new Queue(6L, "token-6", QueueStatus.WAITING, now.plusMinutes(5)),
                        new Queue(7L, "token-7", QueueStatus.WAITING, now.plusMinutes(4))
                );

        for(Queue queue : queueList) {
            queueRepository.save(queue);
        }

        // 테스트 대상 유저 추가 (대기 중)
        Queue newQueue = new Queue(8L, "token-8", QueueStatus.WAITING, now.plusMinutes(3));
        queueRepository.save(newQueue);

        // when: 대기열 상태를 확인
        SelectQueueTokenResult result = queueService.checkQueue(newQueue.getToken());

        // then: 새 유저가 대기 중인 순번을 확인, 진행 중 상태로 변경됨
        Queue updatedQueue = queueRepository.findByToken(newQueue.getToken());
        assertThat(updatedQueue.getStatus()).isEqualTo(QueueStatus.PROGRESS);  // 상태가 PROGRESS로 변경됨
        assertThat(result.queuePosition()).isEqualTo(0);  // 대기 순번 0 반환 확인
    }

    @Test
    void 대기열_상태가_EXPIRED일_경우_예외가_발생한다() {
        // given: 대기열 상태가 만료(EXPIRED) 상태인 경우
        Queue expiredQueue = new Queue(1L, "expired-token", QueueStatus.EXPIRED, LocalDateTime.now().minusMinutes(1));
        queueRepository.save(expiredQueue);

        // when & then: 상태가 만료(EXPIRED)된 유저가 접근하면 예외 발생
        assertThatThrownBy(() -> queueService.checkQueue(expiredQueue.getToken()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대기열 상태가 활성상태가 아닙니다.");
    }
}
