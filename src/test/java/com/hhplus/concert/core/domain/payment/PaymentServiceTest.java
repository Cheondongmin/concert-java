package com.hhplus.concert.core.domain.payment;

import com.hhplus.concert.IntegrationTest;
import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.domain.reservation.*;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PaymentServiceTest extends IntegrationTest {

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
    private PaymentService paymentService;

    @Autowired
    private ReservationService reservationService;

    @Nested
    class IntegrationTests {
        @Test
        void 잔액이_1000원인_유저가_500원_콘서트를_예매하면_잔액이_500원_남고_좌석상태가_예약완료된다() {
            // given
            Users user = new Users(1L, 1000L); // 유저 잔액은 1000원
            userRepository.save(user);
            String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
            Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
            queueRepository.save(queue);

            // 500원 콘서트 설정
            Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
            concertRepository.save(concert);
            ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDateTime.now().toLocalDate(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
            concertScheduleRepository.save(concertSchedule);
            ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
            concertSeatRepository.save(concertSeat);

            // when
            // 예약 진행
            ReserveConcertResult reserveResult = reservationService.reserveConcert(queue.getToken(), concertSchedule.getId(), concertSeat.getId());

            // then (예약 완료 확인)
            Reservation reservation = reservationRepository.findById(reserveResult.reservationId());
            assertThat(reservation.getSeatAmount()).isEqualTo(500L); // 예약 금액이 500원
            assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.TEMP_RESERVED); // 임시 예약 상태

            // when (결제 진행)
            paymentService.paymentConcert(queue.getToken(), reservation.getId());

            // then (결제 후 확인)
            Users updatedUser = userRepository.findById(user.getId());
            ConcertSeat updatedSeat = concertSeatRepository.findById(concertSeat.getId());
            Payment payment = paymentRepository.findByReservationId(reservation.getId());

            assertThat(updatedUser.getUserAmount()).isEqualTo(500L); // 1000원에서 500원이 차감되어 500원 남음
            assertThat(updatedSeat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED); // 좌석이 예약완료 상태로 변경
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DONE); // 결제가 완료 상태
        }
    }

    @Nested
    class ConcurrencyTests {
        @Test
        void 잔액이_1000원인_유저가_500원_콘서트를_동시에_10번_결제시도해도_한번만_결제후_잔액은_500원_남는다() throws InterruptedException {
            // given
            Users user = new Users(1L, 1000L); // 유저 잔액은 1000원
            userRepository.save(user);
            String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
            Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
            queueRepository.save(queue);

            // 500원 콘서트 설정
            Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
            concertRepository.save(concert);
            ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDate.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
            concertScheduleRepository.save(concertSchedule);
            ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
            concertSeatRepository.save(concertSeat);

            ReserveConcertResult result = reservationService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId());

            int threadCount = 10; // 10번의 동시 결제 시도
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when (10번의 동시 결제 시도)
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        paymentService.paymentConcert(token, result.reservationId());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            // then
            Users updatedUser = userRepository.findById(user.getId());
            ConcertSeat updatedSeat = concertSeatRepository.findById(concertSeat.getId());
            List<Payment> paymentList = paymentRepository.findAll();

            assertThat(paymentList.size()).isEqualTo(1); // 결제는 한 번만 이루어짐
            assertThat(paymentList.get(0).getPrice()).isEqualTo(500L); // 결제 금액은 500원
            assertThat(updatedUser.getUserAmount()).isEqualTo(500L); // 1000원에서 500원이 한 번만 차감됨
            assertThat(updatedSeat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED); // 좌석은 예약완료 상태
        }

        @Test
        void 동일_유저가_100번_결제요청을_해도_한번만_정상적으로_처리된다() throws InterruptedException {
            // given
            Users user = new Users(1L, 1000L); // 유저 잔액은 1000
            userRepository.save(user);
            String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
            Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
            queueRepository.save(queue);

            // 콘서트, 콘서트 스케줄 및 좌석 설정
            Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
            concertRepository.save(concert);
            ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDate.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
            concertScheduleRepository.save(concertSchedule);
            ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
            concertSeatRepository.save(concertSeat);

            // 예약 생성 및 저장
            ReserveConcertResult result = reservationService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId());

            // 동시성 제어를 위한 ExecutorService 설정
            int threadCount = 100; // 동일한 유저가 100번 결제 요청
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when
            for (int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        paymentService.paymentConcert(token, result.reservationId());
                    } finally {
                        latch.countDown(); // 스레드가 종료되면 카운트 감소
                    }
                });
            }

            latch.await(); // 모든 스레드가 완료될 때까지 대기

            // then
            Users updatedUser = userRepository.findById(user.getId());
            ConcertSeat updatedSeat = concertSeatRepository.findById(concertSeat.getId());
            List<Payment> paymentList = paymentRepository.findAll();

            // 한 번만 결제가 성공적으로 완료되어야 함]
            assertThat(paymentList.size()).isEqualTo(1); // payment는 한번만 등록되어야 함
            assertThat(paymentList.get(0).getPrice()).isEqualTo(500L);
            assertThat(updatedUser.getUserAmount()).isEqualTo(500L); // 잔액이 500으로 감소해야 함
            assertThat(updatedSeat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED); // 좌석 상태가 RESERVED로 변경
        }
    }
}