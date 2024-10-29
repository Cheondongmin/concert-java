package com.hhplus.concert.core.domain.reservation;

import com.hhplus.concert.core.domain.concert.Concert;
import com.hhplus.concert.core.domain.concert.ConcertSchedule;
import com.hhplus.concert.core.domain.concert.ConcertSeat;
import com.hhplus.concert.core.domain.concert.SeatStatus;
import com.hhplus.concert.core.domain.concert.TotalSeatStatus;
import com.hhplus.concert.core.domain.user.Users;
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
            String concertTitle = "테스트 콘서트";
            LocalDate openDt = LocalDate.now();
            LocalDateTime startDt = LocalDateTime.now().plusHours(1);
            LocalDateTime endDt = startDt.plusHours(2);
            Long seatAmount = 50000L;
            Integer seatPosition = 1;

            // when
            Reservation reservation = new Reservation(
                    userId, seatId, concertTitle, openDt, startDt, endDt,
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

        @Test
        void enterReservation_정적_팩토리_메서드로_예약_객체가_생성된다() {
            // given
            Users user = new Users(1L, 100000L);
            Concert concert = new Concert(1L, "테스트 콘서트", LocalDateTime.now(), false);
            ConcertSchedule schedule = new ConcertSchedule(
                    1L, concert.getId(), LocalDate.now(),
                    LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                    100, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false
            );
            ConcertSeat seat = new ConcertSeat(
                    1L, schedule.getId(), 50000L, 1,
                    SeatStatus.AVAILABLE, null, LocalDateTime.now(), false
            );

            // when
            Reservation reservation = Reservation.enterReservation(user, concert, seat, schedule);

            // then
            assertAll(
                    () -> assertThat(reservation.getUserId()).isEqualTo(user.getId()),
                    () -> assertThat(reservation.getSeatId()).isEqualTo(seat.getId()),
                    () -> assertThat(reservation.getConcertTitle()).isEqualTo(concert.getTitle()),
                    () -> assertThat(reservation.getConcertOpenDt()).isEqualTo(schedule.getOpenDt()),
                    () -> assertThat(reservation.getConcertStartDt()).isEqualTo(schedule.getStartDt()),
                    () -> assertThat(reservation.getConcertEndDt()).isEqualTo(schedule.getEndDt()),
                    () -> assertThat(reservation.getSeatAmount()).isEqualTo(seat.getAmount()),
                    () -> assertThat(reservation.getSeatPosition()).isEqualTo(seat.getPosition()),
                    () -> assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.TEMP_RESERVED)
            );
        }
    }

    @Nested
    class StatusChangeTests {
        @Test
        void TEMP_RESERVED_상태에서_RESERVED_상태로_변경된다() {
            // given
            Reservation reservation = new Reservation(
                    1L, 1L, "테스트 콘서트",
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
                    1L, 1L, "테스트 콘서트",
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