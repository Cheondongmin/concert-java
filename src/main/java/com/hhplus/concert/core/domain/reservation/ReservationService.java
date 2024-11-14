package com.hhplus.concert.core.domain.reservation;

import com.hhplus.concert.core.domain.concert.*;
import com.hhplus.concert.core.domain.event.reservation.ReservationCreatedEvent;
import com.hhplus.concert.core.domain.payment.Payment;
import com.hhplus.concert.core.domain.payment.PaymentRepository;
import com.hhplus.concert.core.domain.payment.PaymentStatus;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ApplicationEventPublisher eventPublisher;

    protected ReservationValidationResult validateReservation(String token, long scheduleId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);
        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        ConcertSchedule schedule = concertScheduleRepository.findById(scheduleId);
        schedule.isSoldOutCheck();

        return new ReservationValidationResult(userId, user, queue, schedule);
    }

    @Transactional
    @Retryable(
            retryFor = PessimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 200)
    )
    public ReserveConcertResult reserveConcert(String token, long scheduleId, long seatId) {
        // 1. 검증
        ReservationValidationResult validationResult = validateReservation(token, scheduleId);

        // 2. 좌석 예약
        ConcertSeat concertSeat = concertSeatRepository.findAvailableSeatWithLock(seatId);
        concertSeat.isReserveCheck();

        // 3. 예약 생성
        Concert concert = concertRepository.findById(validationResult.concertSchedule().getConcertId());
        Reservation reservation = Reservation.enterReservation(
                validationResult.user(),
                concert,
                concertSeat,
                validationResult.concertSchedule()
        );
        reservationRepository.save(reservation);

        // 4. 결제 대기 상태 생성
        Payment payment = Payment.enterPayment(
                validationResult.userId(),
                reservation.getId(),
                concertSeat.getAmount(),
                PaymentStatus.PROGRESS
        );
        paymentRepository.save(payment);

        // 5. 이벤트 발행
        eventPublisher.publishEvent(new ReservationCreatedEvent(
                validationResult.userId(),
                reservation.getId(),
                seatId,
                scheduleId,
                concertSeat.getAmount()
        ));

        return new ReserveConcertResult(reservation.getId(),
                reservation.getStatus(),
                reservation.getReservedDt(),
                reservation.getReservedUntilDt());
    }
}