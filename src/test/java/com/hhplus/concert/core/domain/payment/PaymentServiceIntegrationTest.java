package com.hhplus.concert.core.domain.payment;

import com.hhplus.concert.IntegrationTest;
import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.domain.reservation.Reservation;
import com.hhplus.concert.core.domain.reservation.ReservationRepository;
import com.hhplus.concert.core.domain.reservation.ReservationStatus;
import com.hhplus.concert.core.domain.reservation.ReserveConcertResult;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PaymentServiceIntegrationTest extends IntegrationTest {
    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertService concertService;

    @Autowired
    private PaymentService paymentService;

    @Test
    void 결제를_진행하고_잔액과_좌석상태가_변경된다() {
        // given
        Users user = new Users(1L, 1000L); // 유저 잔액은 1000
        userRepository.save(user);
        String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
        Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
        queueRepository.save(queue);

        // 콘서트, 콘서트 스케줄 및 좌석 설정
        Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
        concertRepository.save(concert);
        ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDateTime.now().toLocalDate(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
        concertScheduleRepository.save(concertSchedule);
        ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
        concertSeatRepository.save(concertSeat);

        // when
        // 예약 진행
        ReserveConcertResult reserveResult = concertService.reserveConcert(queue.getToken(), concertSchedule.getId(), concertSeat.getId());

        // then (예약 완료 확인)
        Reservation reservation = reservationRepository.findById(reserveResult.reservationId());
        assertThat(reservation.getSeatAmount()).isEqualTo(500L); // 예약 금액 확인
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.TEMP_RESERVED); // 예약 상태 확인

        // when (결제 진행)
        paymentService.paymentConcert(queue.getToken(), reservation.getId());

        // then (결제 후 잔액 및 좌석 상태 확인)
        Users updatedUser = userRepository.findById(user.getId());
        ConcertSeat updatedSeat = concertSeatRepository.findById(concertSeat.getId());
        Payment payment = paymentRepository.findByReservationId(reservation.getId());

        assertThat(updatedUser.getUserAmount()).isEqualTo(500L); // 결제 후 잔액이 500으로 감소
        assertThat(updatedSeat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED); // 좌석 상태가 RESERVED로 변경
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE); // 결제 상태가 완료로 변경
    }
}
