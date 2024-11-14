package com.hhplus.concert.core.domain.reservation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ReservationUnitTest {

    @Nested
    class CreationTests {
        @Test
        void 예약_객체가_정상적으로_생성된다() {
            // given
            Long userId = 1L;
            Long seatId = 1L;
            Long concertScheduleId = 1L;
            String concertTitle = "테스트 콘서트";
            LocalDate openDt = LocalDate.now();
            LocalDateTime startDt = LocalDateTime.now().plusHours(1);
            LocalDateTime endDt = startDt.plusHours(2);
            Long seatAmount = 50000L;
            Integer seatPosition = 1;

            // when
            Reservation reservation = new Reservation(
                    userId, seatId, concertScheduleId, concertTitle, openDt, startDt, endDt,
                    seatAmount, seatPosition
            );

            // then
            assertAll(
                    () -> assertThat(reservation.getUserId()).isEqualTo(userId),
                    () -> assertThat(reservation.getSeatId()).isEqualTo(seatId),
                    () -> assertThat(reservation.getConcertTitle()).isEqualTo(concertTitle),
                    () -> assertThat(reservation.getConcertOpenDt()).isEqualTo(openDt),
                    () -> assertThat(reservation.getConcertStartDt()).isEqualTo(startDt),
                    () -> assertThat(reservation.getConcertEndDt()).isEqualTo(endDt),
                    () -> assertThat(reservation.getSeatAmount()).isEqualTo(seatAmount),
                    () -> assertThat(reservation.getSeatPosition()).isEqualTo(seatPosition),
                    () -> assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.TEMP_RESERVED),
                    () -> assertThat(reservation.getReservedUntilDt())
                            .isAfter(LocalDateTime.now())
                            .isBefore(LocalDateTime.now().plusMinutes(6)),
                    () -> assertThat(reservation.getIsDelete()).isFalse()
            );
        }
    }

    @Nested
    class StatusChangeTests {
        @Test
        void TEMP_RESERVED_상태에서_RESERVED_상태로_변경된다() {
            // given
            Reservation reservation = new Reservation(
                    1L, 1L, 1L, "테스트 콘서트",
                    LocalDate.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                    50000L, 1
            );
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.TEMP_RESERVED);

            // when
            reservation.finishReserve();

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        }

        @Test
        void 이미_RESERVED_상태면_상태가_변경되지_않는다() {
            // given
            Reservation reservation = new Reservation(
                    1L, 1L, 1L, "테스트 콘서트",
                    LocalDate.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                    50000L, 1
            );
            reservation.finishReserve(); // RESERVED 상태로 먼저 변경
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);

            // when
            reservation.finishReserve(); // 한 번 더 상태 변경 시도

            // then
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        }
    }
}