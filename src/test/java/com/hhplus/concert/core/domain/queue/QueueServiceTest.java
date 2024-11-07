package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.IntegrationTest;
import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class QueueServiceTest extends IntegrationTest {

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private QueueService queueService;

    @Nested
    class EnterQueueTests {
        @Test
        void 신규유저는_대기열에_등록되고_토큰을_발급받는다() {
            // given
            Long userId = 1L;

            // when
            String queueToken = queueService.enterQueue(userId);

            // then
            List<Queue> userQueues = queueRepository.findAll();
            assertAll(() -> assertThat(queueToken).isEqualTo(userQueues.get(0).getToken()), () -> AssertionsForInterfaceTypes.assertThat(userQueues).hasSize(1));
        }

        @Test
        void 기존유저는_이미_발급된_토큰을_다시_받는다() {
            // given
            String existQueueToken = "existQueueToken";
            Long userId = 1L;
            queueRepository.save(new Queue(userId, existQueueToken));

            // when
            String queueToken = queueService.enterQueue(userId);

            // then
            List<Queue> userQueues = queueRepository.findAll();
            assertAll(() -> assertThat(existQueueToken).isEqualTo(queueToken), () -> AssertionsForInterfaceTypes.assertThat(userQueues).hasSize(1));
        }
    }

    @Nested
    class CheckQueueStatusTests {
        @Test
        void 대기인원이_30명_미만이면_PROGRESS_상태로_변경되고_대기순번_0을_반환한다() {
            // given
            Long userId = 1L;
            String token = Queue.generateJwtToken(userId);
            Queue queue = new Queue(userId, token, QueueStatus.WAITING, null);
            queueRepository.save(queue);

            List<Queue> waitingQueueList = queueRepository.findOrderByDescByStatus(QueueStatus.WAITING);
            assertThat(waitingQueueList.size()).isLessThan(30);

            // when
            SelectQueueTokenResult result = queueService.checkQueue(queue.getToken());

            // then
            assertAll(() -> assertThat(result.status()).isEqualTo(QueueStatus.PROGRESS), () -> assertThat(result.queuePosition()).isEqualTo(0L));
        }

        @Test
        void 대기인원이_30명_이상이면_WAITING_상태를_유지하고_대기순번을_반환한다() {
            // given
            // 대기열 30명 생성
            for (int i = 1; i <= 30; i++) {
                String token = Queue.generateJwtToken((long) i);
                Queue otherQueue = new Queue((long) i, token, QueueStatus.WAITING, null);
                queueRepository.save(otherQueue);
            }

            // 31번째 유저 생성
            Queue queue = new Queue(31L, Queue.generateJwtToken(31L), QueueStatus.WAITING, null);
            queueRepository.save(queue);

            // when
            List<Queue> waitingQueueList = queueRepository.findOrderByDescByStatus(QueueStatus.WAITING);
            SelectQueueTokenResult result = queueService.checkQueue(queue.getToken());

            // then
            assertAll(() -> assertThat(waitingQueueList.size()).isGreaterThanOrEqualTo(30), () -> assertThat(result.status()).isEqualTo(QueueStatus.WAITING), () -> assertThat(result.queuePosition()).isEqualTo(31L));
        }

        @Test
        void 진행중_인원이_5명_미만이면_대기유저가_PROGRESS_상태가_된다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            List<Queue> queueList = List.of(
                    new Queue(1L, Queue.generateJwtToken(1L), QueueStatus.PROGRESS, now.plusMinutes(10)),
                    new Queue(2L, Queue.generateJwtToken(2L), QueueStatus.PROGRESS, now.plusMinutes(9)),
                    new Queue(3L, Queue.generateJwtToken(3L), QueueStatus.PROGRESS, now.plusMinutes(8)),
                    new Queue(4L, Queue.generateJwtToken(4L), QueueStatus.PROGRESS, now.plusMinutes(7)),
                    new Queue(5L, Queue.generateJwtToken(5L), QueueStatus.PROGRESS, now.plusMinutes(6)),
                    new Queue(6L, Queue.generateJwtToken(6L), QueueStatus.WAITING, now.plusMinutes(5)),
                    new Queue(7L, Queue.generateJwtToken(7L), QueueStatus.WAITING, now.plusMinutes(4)));

            for (Queue queue : queueList) {
                queueRepository.save(queue);
            }

            Queue newQueue = new Queue(8L, Queue.generateJwtToken(8L), QueueStatus.WAITING, now.plusMinutes(3));
            queueRepository.save(newQueue);

            // when
            SelectQueueTokenResult result = queueService.checkQueue(newQueue.getToken());

            // then
            Queue updatedQueue = queueRepository.findByToken(newQueue.getToken());
            assertThat(updatedQueue.getStatus()).isEqualTo(QueueStatus.PROGRESS);
            assertThat(result.queuePosition()).isEqualTo(0);
        }

        @Test
        void EXPIRED_상태의_토큰은_예외가_발생한다() {
            // given
            Queue expiredQueue = new Queue(1L, Queue.generateJwtToken(1L), QueueStatus.EXPIRED, LocalDateTime.now().minusMinutes(1));
            queueRepository.save(expiredQueue);

            // when & then
            assertThatThrownBy(() -> queueService.checkQueue(expiredQueue.getToken())).isInstanceOf(ApiException.class).hasMessage("대기열 상태가 활성상태가 아닙니다.");
        }
    }

    @Nested
    class SchedulerTests {
        @Test
        void 스케줄러는_WAITING_상태의_유저들을_PROGRESS_상태로_변경한다() {
            // given
            queueRepository.save(new Queue(1L, Queue.generateJwtToken(1L), QueueStatus.WAITING, null));
            queueRepository.save(new Queue(2L, Queue.generateJwtToken(2L), QueueStatus.WAITING, null));

            // when
            queueService.periodicallyEnterUserQueue();

            // then
            List<Queue> progressedQueues = queueRepository.findOrderByDescByStatus(QueueStatus.PROGRESS);
            assertEquals(2, progressedQueues.size());
        }
    }
}