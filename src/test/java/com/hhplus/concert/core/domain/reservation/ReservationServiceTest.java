package com.hhplus.concert.core.domain.reservation;

import com.hhplus.concert.IntegrationTest;
import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.domain.payment.Payment;
import com.hhplus.concert.core.domain.payment.PaymentRepository;
import com.hhplus.concert.core.domain.payment.PaymentService;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import org.assertj.core.api.AssertionsForInterfaceTypes;
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
public class ReservationServiceTest extends IntegrationTest {

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
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationService concertService;

    @Autowired
    private PaymentService paymentService;

    @Nested
    class ConcurrencyTests {
        @Test
        void 좌석_임시예약_동시성_테스트() throws InterruptedException {
            // given
            int numberOfThreads = 100;
            ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

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

            for (int i = 0; i < numberOfThreads; i++) {
                service.execute(() -> {
                    try {
                        // when
                        concertService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();

            // then
            List<Reservation> reservations = reservationRepository.findAll();
            AssertionsForInterfaceTypes.assertThat(reservations).hasSize(1);
        }

        @Test
        void 동시에_5명의_유저가_동일_좌석을_예약하려고_할_때_한번만_정상적으로_처리된다() throws InterruptedException {
            // given
            // 5명의 유저 생성 및 저장
            Users user1 = new Users(1L, 1000L);
            Users user2 = new Users(2L, 1000L);
            Users user3 = new Users(3L, 1000L);
            Users user4 = new Users(4L, 1000L);
            Users user5 = new Users(5L, 1000L);
            List<Users> usersList = List.of(user1, user2, user3, user4, user5);
            for (Users user : usersList) {
                userRepository.save(user);
            }

            String token1 = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
            String token2 = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjIsInRva2VuIjoiNTQ0ODAyMjUtZmI4Mi00OTRlLThkZDQtOGE1NTYzYjBlN2EwIiwiZW50ZXJlZER0IjoxNzI5MTg3NzE4NzIyLCJleHBpcmVkRHQiOjE3MjkxODgwMTg3MjJ9.";
            String token3 = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjMsInRva2VuIjoiOWUyMTk1NWMtMjY5YS00ZDllLWFmYzYtOTljOTE1NTg4NTFiIiwiZW50ZXJlZER0IjoxNzI5MTg3ODM1MzgzLCJleHBpcmVkRHQiOjE3MjkxODgxMzUzODN9.";
            String token4 = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjQsInRva2VuIjoiNTRiOTdiNmEtNjJjMS00NDg0LTg4NjgtNzc5OTUxYTA4YTBjIiwiZW50ZXJlZER0IjoxNzI5MTg3ODQ5MzMwLCJleHBpcmVkRHQiOjE3MjkxODgxNDkzMzB9.";
            String token5 = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjUsInRva2VuIjoiODg5ZGI4OGQtNzVlZS00MTFlLWI3MzAtOTg3NmIyYTk0MWFhIiwiZW50ZXJlZER0IjoxNzI5MTg3ODY3OTExLCJleHBpcmVkRHQiOjE3MjkxODgxNjc5MTF9.";

            // 큐 생성 및 저장
            Queue queue1 = new Queue(user1.getId(), token1, QueueStatus.PROGRESS, LocalDateTime.now().plusMinutes(10));
            Queue queue2 = new Queue(user2.getId(), token2, QueueStatus.PROGRESS, LocalDateTime.now().plusMinutes(10));
            Queue queue3 = new Queue(user3.getId(), token3, QueueStatus.PROGRESS, LocalDateTime.now().plusMinutes(10));
            Queue queue4 = new Queue(user4.getId(), token4, QueueStatus.PROGRESS, LocalDateTime.now().plusMinutes(10));
            Queue queue5 = new Queue(user5.getId(), token5, QueueStatus.PROGRESS, LocalDateTime.now().plusMinutes(10));
            List<Queue> queueList = List.of(queue1, queue2, queue3, queue4, queue5);
            for (Queue queue : queueList) {
                queueRepository.save(queue);
            }

            // 콘서트와 좌석 설정 및 저장
            Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
            concertRepository.save(concert);
            ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDate.now(), LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(5), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
            concertScheduleRepository.save(concertSchedule);
            ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
            concertSeatRepository.save(concertSeat);

            // 동시성 제어를 위한 ExecutorService 설정
            int threadCount = 5;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // when
            executorService.submit(() -> {
                try {
                    concertService.reserveConcert(token1, concertSchedule.getId(), concertSeat.getId());
                } finally {
                    latch.countDown(); // 스레드가 종료되면 카운트 감소
                }
            });

            executorService.submit(() -> {
                try {
                    concertService.reserveConcert(token2, concertSchedule.getId(), concertSeat.getId());
                } finally {
                    latch.countDown(); // 스레드가 종료되면 카운트 감소
                }
            });

            executorService.submit(() -> {
                try {
                    concertService.reserveConcert(token3, concertSchedule.getId(), concertSeat.getId());
                } finally {
                    latch.countDown(); // 스레드가 종료되면 카운트 감소
                }
            });

            executorService.submit(() -> {
                try {
                    concertService.reserveConcert(token4, concertSchedule.getId(), concertSeat.getId());
                } finally {
                    latch.countDown(); // 스레드가 종료되면 카운트 감소
                }
            });

            executorService.submit(() -> {
                try {
                    concertService.reserveConcert(token5, concertSchedule.getId(), concertSeat.getId());
                } finally {
                    latch.countDown(); // 스레드가 종료되면 카운트 감소
                }
            });

            latch.await(); // 모든 스레드가 완료될 때까지 대기

            // then
            ConcertSeat updatedSeat = concertSeatRepository.findById(concertSeat.getId());
            List<Reservation> reservations = reservationRepository.findAll();

            // 하나의 스레드만 성공적으로 좌석을 예약해야 함
            assertThat(reservations).hasSize(1);
            assertThat(updatedSeat.getSeatStatus()).isEqualTo(SeatStatus.TEMP_RESERVED); // 좌석이 예약된 상태로 변경
        }

        @Test
        void 동일_유저가_여러번_결제요청을_해도_한번만_정상적으로_처리된다() throws InterruptedException {
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
    }
}
