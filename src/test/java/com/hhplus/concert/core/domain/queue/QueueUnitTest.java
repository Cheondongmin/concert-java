package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.exception.ExceptionCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueueUnitTest {

    @Nested
    class WaitingQueueTests {
        @Test
        void 대기열이_30명_미만이면_PROGRESS_상태로_변경된다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.WAITING,
                    LocalDateTime.now().plusMinutes(10));
            List<Queue> queueList = Collections.nCopies(29, queue); // 29명의 대기열

            // when
            queue.checkWaitingQueue(queueList);

            // then
            assertEquals(QueueStatus.PROGRESS, queue.getStatus());
        }

        @Test
        void 대기열이_30명_이상이면_WAITING_상태를_유지한다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.WAITING,
                    LocalDateTime.now().plusMinutes(10));
            List<Queue> queueList = Collections.nCopies(31, queue); // 31명의 대기열

            // when
            queue.checkWaitingQueue(queueList);

            // then
            assertEquals(QueueStatus.WAITING, queue.getStatus());
        }
    }

    @Nested
    class TokenValidationTests {
        @Test
        void WAITING_상태이고_만료시간이_없으면_토큰이_유효하다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.WAITING, null);

            // when
            boolean isValid = queue.isTokenValid();

            // then
            assertTrue(isValid);
        }

        @Test
        void WAITING_상태이고_만료시간이_남아있으면_토큰이_유효하다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.WAITING,
                    LocalDateTime.now().plusMinutes(10));

            // when
            boolean isValid = queue.isTokenValid();

            // then
            assertTrue(isValid);
        }

        @Test
        void WAITING_상태이고_만료시간이_지나면_토큰이_유효하지_않다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.WAITING,
                    LocalDateTime.now().minusMinutes(10));

            // when
            boolean isValid = queue.isTokenValid();

            // then
            assertFalse(isValid);
        }

        @Test
        void PROGRESS_상태이고_만료시간이_없으면_토큰이_유효하다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.PROGRESS, null);

            // when
            boolean isValid = queue.isTokenValid();

            // then
            assertTrue(isValid);
        }

        @Test
        void PROGRESS_상태이고_만료시간이_지나면_토큰이_유효하지_않다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.PROGRESS,
                    LocalDateTime.now().minusMinutes(10));

            // when
            boolean isValid = queue.isTokenValid();

            // then
            assertFalse(isValid);
        }

        @Test
        void EXPIRED_상태면_만료시간과_관계없이_토큰이_유효하지_않다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.EXPIRED,
                    LocalDateTime.now().minusMinutes(10));

            // when
            boolean isValid = queue.isTokenValid();

            // then
            assertFalse(isValid);
        }
    }

    @Nested
    class InterceptorTests {
        @Test
        void 빈_토큰은_IllegalArgumentException이_발생한다() {
            // given
            String emptyToken = "";

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> Queue.tokenNullCheck(emptyToken));
            assertEquals("토큰이 존재하지 않습니다.", exception.getMessage());
        }

        @Test
        void 정상_토큰은_검증을_통과한다() {
            // given
            String validToken = "valid-token";

            // when & then
            assertDoesNotThrow(() -> Queue.tokenNullCheck(validToken));
        }

        @Test
        void PROGRESS가_아닌_상태의_토큰은_E403_예외가_발생한다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.WAITING,
                    LocalDateTime.now().plusMinutes(10));

            // when & then
            ApiException exception = assertThrows(ApiException.class, queue::checkToken);
            assertEquals(ExceptionCode.E403, exception.getExceptionCode());
        }

        @Test
        void 만료된_토큰은_E403_예외가_발생한다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.PROGRESS,
                    LocalDateTime.now().minusMinutes(1));

            // when & then
            ApiException exception = assertThrows(ApiException.class, queue::checkToken);
            assertEquals(ExceptionCode.E403, exception.getExceptionCode());
        }

        @Test
        void PROGRESS_상태이고_유효기간_내의_토큰은_검증을_통과한다() {
            // given
            Queue queue = new Queue(1L, "test-token", QueueStatus.PROGRESS,
                    LocalDateTime.now().plusMinutes(10));

            // when & then
            assertDoesNotThrow(queue::checkToken);
        }
    }
}