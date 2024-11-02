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
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final QueueRepository queueRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PlatformTransactionManager transactionManager;
    private final PaymentRedisRock paymentRedisRock;

    public PaymentConcertResult paymentConcertForRedis(String token, long reservationId) {
        String lockKey = "lock:결제:" + token;
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return paymentRedisRock.executeWithLock(
                lockKey,
                10,
                15,
                () -> transactionTemplate.execute(status -> {
                    try {
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
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        throw e;
                    }
                })
        );
    }

    @Transactional
    public PaymentConcertResult paymentConcert(String token, long reservationId) {
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
