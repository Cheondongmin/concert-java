package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final ConcertRepository concertRepository;
    private final QueueRepository queueRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional(readOnly = true)
    public List<SelectConcertResult> selectConcertList(String token) {
        long userId = Users.extractUserIdFromJwt(token);
        userRepository.findById(userId);
        return concertScheduleRepository.findConcertSchedule();
    }

    @Transactional(readOnly = true)
    public List<SelectSeatResult> selectConcertSeatList(String token, long scheduleId) {
        long userId = Users.extractUserIdFromJwt(token);
        userRepository.findById(userId);
        return concertSeatRepository.findConcertSeat(scheduleId);
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
    public PaymentConcertResult paymentConcert(String token, long reservationId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findByIdWithLock(userId);

        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        Reservation reservation = reservationRepository.findById(reservationId);

        user.checkConcertAmount(reservation.getSeatAmount());

        // 비관적 락을 사용하여 좌석 조회 및 예약 처리
        ConcertSeat concertSeat = concertSeatRepository.findByIdWithLock(reservation.getSeatId());
        concertSeat.finishSeatReserve();

        queue.finishQueue();

        Payment payment = paymentRepository.findByReservationId(reservation.getId());
        payment.finishPayment();

        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(userId, payment.getPrice(), PaymentType.PAYMENT, payment.getId());
        paymentHistoryRepository.save(paymentHistory);

        return new PaymentConcertResult(concertSeat.getAmount(), reservation.getStatus(), queue.getStatus());
    }
}
