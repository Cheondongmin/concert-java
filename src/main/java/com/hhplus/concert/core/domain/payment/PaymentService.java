package com.hhplus.concert.core.domain.payment;

import com.hhplus.concert.core.domain.concert.ConcertSeat;
import com.hhplus.concert.core.domain.concert.ConcertSeatRepository;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.reservation.Reservation;
import com.hhplus.concert.core.domain.reservation.ReservationRepository;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final QueueRepository queueRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional
    public PaymentConcertResult paymentConcert(String token, long reservationId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);

        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        Reservation reservation = reservationRepository.findById(reservationId);
        user.checkConcertAmount(reservation.getSeatAmount());

        // 비관적 락을 사용하여 좌석 조회 및 예약 처리
        ConcertSeat concertSeat = concertSeatRepository.findByIdWithLock(reservation.getSeatId());
        concertSeat.finishSeatReserve();
        queue.finishQueue();

        reservation.finishReserve();

        Payment payment = paymentRepository.findByReservationId(reservation.getId());
        payment.finishPayment();

        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(userId, payment.getPrice(), PaymentType.PAYMENT, payment.getId());
        paymentHistoryRepository.save(paymentHistory);

        return new PaymentConcertResult(concertSeat.getAmount(), reservation.getStatus(), queue.getStatus());
    }

    @Transactional
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 10,
            backoff = @Backoff(delay = 200)
    )
    public PaymentConcertResult paymentConcertWithOptimisticLock(String token, long reservationId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);

        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        Reservation reservation = reservationRepository.findById(reservationId);
        user.checkConcertAmount(reservation.getSeatAmount());

        // 낙관적 락을 사용하여 좌석 조회 및 예약 처리
        ConcertSeat concertSeat = concertSeatRepository.findById(reservation.getSeatId());
        concertSeat.finishSeatReserve();

        queue.finishQueue();
        reservation.finishReserve();

        Payment payment = paymentRepository.findByReservationId(reservation.getId());
        payment.finishPayment();

        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(userId, payment.getPrice(), PaymentType.PAYMENT, payment.getId());
        paymentHistoryRepository.save(paymentHistory);

        return new PaymentConcertResult(concertSeat.getAmount(), reservation.getStatus(), queue.getStatus());
    }
}
