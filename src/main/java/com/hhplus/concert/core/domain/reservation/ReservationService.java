package com.hhplus.concert.core.domain.reservation;

import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.domain.payment.Payment;
import com.hhplus.concert.core.domain.payment.PaymentRepository;
import com.hhplus.concert.core.domain.payment.PaymentStatus;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final ConcertRepository concertRepository;
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final RedissonClient redissonClient;

    @Transactional
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 10,
            backoff = @Backoff(delay = 200)
    )
    public ReserveConcertResult reserveConcertWithOptimisticLock(String token, long scheduleId, long seatId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);

        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        ConcertSchedule concertSchedule = concertScheduleRepository.findById(scheduleId);
        concertSchedule.isSoldOutCheck();

        // 낙관적 락을 사용하여 좌석 조회 및 예약 처리
        ConcertSeat concertSeat = concertSeatRepository.findById(seatId);
        concertSeat.isReserveCheck();

        Concert concert = concertRepository.findById(concertSchedule.getConcertId());
        Reservation reservation = Reservation.enterReservation(user, concert, concertSeat, concertSchedule);
        reservationRepository.save(reservation);

        Payment payment = Payment.enterPayment(userId, reservation.getId(), concertSeat.getAmount(), PaymentStatus.PROGRESS);
        paymentRepository.save(payment);

        return new ReserveConcertResult(reservation.getId(), reservation.getStatus(), reservation.getReservedDt(), reservation.getReservedUntilDt());
    }

    @Transactional
    public ReserveConcertResult reserveConcert(String token, long scheduleId, long seatId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);

        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        ConcertSchedule concertSchedule = concertScheduleRepository.findById(scheduleId);
        concertSchedule.isSoldOutCheck();

        // 비관적 락을 사용하여 좌석 조회 및 예약 처리
        ConcertSeat concertSeat = concertSeatRepository.findByIdWithLock(seatId);
        concertSeat.isReserveCheck();

        Concert concert = concertRepository.findById(concertSchedule.getConcertId());
        Reservation reservation = Reservation.enterReservation(user, concert, concertSeat, concertSchedule);
        reservationRepository.save(reservation);

        Payment payment = Payment.enterPayment(userId, reservation.getId(), concertSeat.getAmount(), PaymentStatus.PROGRESS);
        paymentRepository.save(payment);

        return new ReserveConcertResult(reservation.getId(), reservation.getStatus(), reservation.getReservedDt(), reservation.getReservedUntilDt());
    }

    @Transactional
    public ReserveConcertResult reserveConcertWithRedis(String token, long scheduleId, long seatId) {
        RLock lock = redissonClient.getLock("seat:" + seatId);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("좌석 예약에 실패했습니다. 다시 시도해주세요.");
            }

            long userId = Users.extractUserIdFromJwt(token);
            Users user = userRepository.findById(userId);

            Queue queue = queueRepository.findByToken(token);
            queue.tokenReserveCheck();

            ConcertSchedule concertSchedule = concertScheduleRepository.findById(scheduleId);
            concertSchedule.isSoldOutCheck();

            ConcertSeat concertSeat = concertSeatRepository.findById(seatId);
            concertSeat.isReserveCheck();

            Concert concert = concertRepository.findById(concertSchedule.getConcertId());
            Reservation reservation = Reservation.enterReservation(user, concert, concertSeat, concertSchedule);
            reservationRepository.save(reservation);

            Payment payment = Payment.enterPayment(userId, reservation.getId(), concertSeat.getAmount(), PaymentStatus.PROGRESS);
            paymentRepository.save(payment);

            return new ReserveConcertResult(
                    reservation.getId(),
                    reservation.getStatus(),
                    reservation.getReservedDt(),
                    reservation.getReservedUntilDt()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("예약 처리 중 오류가 발생했습니다.", e);
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
