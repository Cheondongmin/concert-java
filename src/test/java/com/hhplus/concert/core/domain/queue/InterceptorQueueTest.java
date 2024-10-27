package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.exception.ExceptionCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InterceptorQueueTest {
    @Test
    void 토큰이_존재하지_않으면_예외가_발생한다() {
        // given
        String token = ""; // 빈 토큰

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Queue.tokenNullCheck(token);
        });
        assertEquals("토큰이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    void 토큰이_존재하면_예외가_발생하지_않는다() {
        // given
        String token = "valid-token";

        // when & then
        assertDoesNotThrow(() -> Queue.tokenNullCheck(token));
    }

    @Test
    void 상태가_PROGRESS가_아니면_ApiException이_발생한다() {
        // given
        Queue queue = new Queue(1L, "test-token", QueueStatus.WAITING, LocalDateTime.now().plusMinutes(10));

        // when & then
        ApiException exception = assertThrows(ApiException.class, queue::checkToken);

        assertEquals(ExceptionCode.E403, exception.getExceptionCode());
    }

    @Test
    void 만료_시간이_지나면_ApiException이_발생한다() {
        // given
        Queue queue = new Queue(1L, "test-token", QueueStatus.PROGRESS, LocalDateTime.now().minusMinutes(1)); // 만료된 토큰

        // when & then
        ApiException exception = assertThrows(ApiException.class, queue::checkToken);

        assertEquals(ExceptionCode.E403, exception.getExceptionCode());
    }

    @Test
    void 상태가_PROGRESS이고_만료되지_않으면_예외가_발생하지_않는다() {
        // given
        Queue queue = new Queue(1L, "test-token", QueueStatus.PROGRESS, LocalDateTime.now().plusMinutes(10)); // 유효한 토큰

        // when & then
        assertDoesNotThrow(queue::checkToken);
    }
}
