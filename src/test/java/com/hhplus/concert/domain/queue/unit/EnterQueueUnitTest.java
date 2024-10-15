package com.hhplus.concert.domain.queue.unit;

import com.hhplus.concert.app.domain.queue.entlty.Queue;
import com.hhplus.concert.app.domain.queue.entlty.QueueStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.hhplus.concert.fixture.queue.QueueFixture.신규유저_큐;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class EnterQueueUnitTest {
    // 유저 대기열 토큰 발급 API 유닛 테스트 코드

    @Test
    void 토큰유효성검사_상태가Waiting이고_만료시간없음() {
        // given
        Queue queue = 신규유저_큐(1L, QueueStatus.WAITING, null);

        // when
        boolean isValid = queue.isTokenValid();

        // then
        assertTrue(isValid); // 토큰이 유효해야 함
    }

    @Test
    void 토큰유효성검사_상태가Waiting이고_만료시간이현재시간이후() {
        // given
        Queue queue = 신규유저_큐(1L, QueueStatus.WAITING, LocalDateTime.now().plusMinutes(10));

        // when
        boolean isValid = queue.isTokenValid();

        // then
        assertTrue(isValid); // 토큰이 유효해야 함
    }

    @Test
    void 토큰유효성검사_상태가Waiting이고_만료시간이지났음() {
        // given
        Queue queue = 신규유저_큐(1L, QueueStatus.WAITING, LocalDateTime.now().minusMinutes(10));
        // when
        boolean isValid = queue.isTokenValid();

        // then
        assertFalse(isValid); // 토큰이 유효하지 않음
    }

    @Test
    void 토큰유효성검사_상태가Progress이고_만료시간없음() {
        // given
        Queue queue = 신규유저_큐(1L, QueueStatus.PROGRESS, null);

        // when
        boolean isValid = queue.isTokenValid();

        // then
        assertTrue(isValid); // 토큰이 유효해야 함
    }

    @Test
    void 토큰유효성검사_상태가Progress이고_만료시간이지났음() {
        // given
        Queue queue = 신규유저_큐(1L, QueueStatus.PROGRESS, LocalDateTime.now().minusMinutes(10));

        // when
        boolean isValid = queue.isTokenValid();

        // then
        assertFalse(isValid); // 토큰이 유효해야 함
    }

    @Test
    void 토큰유효성검사_상태가Expired일경우() {
        // given
        Queue queue = 신규유저_큐(1L, QueueStatus.EXPIRED, LocalDateTime.now().minusMinutes(10));

        // when
        boolean isValid = queue.isTokenValid();

        // then
        assertFalse(isValid); // 토큰이 유효하지 않음
    }
}
