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
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    private final ReservationEventPublisher reservationEventPublisher;

    @Transactional
    @Retryable(
            retryFor = PessimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 200)
    )
    public ReserveConcertResult reserveConcert(String token, long scheduleId, long seatId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);

        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        ConcertSchedule concertSchedule = concertScheduleRepository.findById(scheduleId);
        concertSchedule.isSoldOutCheck();

        // 낙관적 락을 사용하여 좌석 조회 및 예약 처리
        ConcertSeat concertSeat = concertSeatRepository.findAvailableSeatWithLock(seatId);
        concertSeat.isReserveCheck();

        Concert concert = concertRepository.findById(concertSchedule.getConcertId());
        Reservation reservation = Reservation.enterReservation(user, concert, concertSeat, concertSchedule);
        reservationRepository.save(reservation);

        Payment payment = Payment.enterPayment(userId, reservation.getId(), concertSeat.getAmount(), PaymentStatus.PROGRESS);
        paymentRepository.save(payment);

        ReservationMessageSendEvent reservationMessageSendEvent = new ReservationMessageSendEvent(user.getUserMail(), concert.getTitle(), concertSchedule.getStartDt(), LocalDateTime.now(), reservation.getReservedUntilDt(), reservation.getSeatAmount());
        reservationEventPublisher.reservedMassageSend(reservationMessageSendEvent);

        return new ReserveConcertResult(reservation.getId(), reservation.getStatus(), reservation.getReservedDt(), reservation.getReservedUntilDt());
    }
}