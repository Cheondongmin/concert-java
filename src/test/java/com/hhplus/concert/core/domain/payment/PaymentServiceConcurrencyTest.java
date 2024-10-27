package com.hhplus.concert.core.domain.payment;

import com.hhplus.concert.IntegrationTest;
import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.domain.reservation.ReservationService;
import com.hhplus.concert.core.domain.reservation.ReserveConcertResult;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
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
public class PaymentServiceConcurrencyTest extends IntegrationTest {

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ConcertService concertService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 동일_유저가_여러번_결제요청을_해도_정상적으로_처리된다() throws InterruptedException {
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
        ReserveConcertResult result = concertService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId());

        // 동시성 제어를 위한 ExecutorService 설정
        int threadCount = 10; // 동일한 유저가 10번 결제 요청
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

    @Test
    void 동일_유저가_여러번_결제요청을_해도_정상적으로_처리된다_낙관적락() throws InterruptedException {
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
        ReserveConcertResult result = reservationService.reserveConcertWithOptimisticLock(token, concertSchedule.getId(), concertSeat.getId());

        // 동시성 제어를 위한 ExecutorService 설정
        int threadCount = 10; // 동일한 유저가 10번 결제 요청
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
