package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConcertServiceUnitTest {

    @Test
    void 콘서트가_매진되어_예약할_수_없다() {
        // given
        ConcertSchedule concertSchedule = new ConcertSchedule(1L, 1L, LocalDate.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), 100, 0, TotalSeatStatus.SOLD_OUT, LocalDateTime.now(), false);

        // when & then
        assertThrows(ApiException.class, concertSchedule::isSoldOutCheck);
    }

    @Test
    void 좌석이_AVAILABLE_상태일_때_예약_가능하다() {
        // given
        ConcertSeat concertSeat = new ConcertSeat(1L, 1L, 500L, 10, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);

        // when & then
        assertDoesNotThrow(concertSeat::isReserveCheck); // 예약 가능해야 함
    }

    @Test
    void 좌석이_RESERVED_상태일_때_예약_불가능하다() {
        // given
        ConcertSeat concertSeat = new ConcertSeat(1L, 1L, 500L, 10, SeatStatus.RESERVED, null, LocalDateTime.now(), false);

        // when & then
        assertThrows(ApiException.class, concertSeat::isReserveCheck, "해당 좌석은 예약할 수 없는 상태 입니다.");
    }

    @Test
    void 좌석이_TEMP_RESERVED_상태일_때_예약_불가능하다() {
        // given
        ConcertSeat concertSeat = new ConcertSeat(1L, 1L, 500L, 10, SeatStatus.TEMP_RESERVED, null, LocalDateTime.now(), false);

        // when & then
        assertThrows(ApiException.class, concertSeat::isReserveCheck, "해당 좌석은 예약할 수 없는 상태 입니다.");
    }
}
