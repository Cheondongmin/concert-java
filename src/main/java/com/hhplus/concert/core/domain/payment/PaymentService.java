package com.hhplus.concert.core.domain.payment;

import com.hhplus.concert.core.domain.concert.ConcertSeat;
import com.hhplus.concert.core.domain.concert.ConcertSeatRepository;
import com.hhplus.concert.core.domain.event.payment.PaymentCompletedEvent;
import com.hhplus.concert.core.domain.event.payment.PaymentHistoryCompensationEvent;
import com.hhplus.concert.core.domain.event.payment.PaymentHistoryInsertEvent;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.reservation.Reservation;
import com.hhplus.concert.core.domain.reservation.ReservationRepository;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    protected PaymentValidationResult validatePayment(String token, long reservationId) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);
        Queue queue = queueRepository.findByToken(token);
        queue.tokenReserveCheck();

        Reservation reservation = reservationRepository.findById(reservationId);
        user.checkConcertAmount(reservation.getSeatAmount());

        return new PaymentValidationResult(userId, user, queue, reservation);
    }

    @Transactional
    public PaymentConcertResult paymentConcert(String token, long reservationId) {
        PaymentHistoryInsertEvent historyEvent = null;

        try {
            // 1. 검증
            PaymentValidationResult validationResult = validatePayment(token, reservationId);

            // 2. 상태 업데이트들
            ConcertSeat concertSeat = concertSeatRepository.findById(validationResult.reservation().getSeatId());
            concertSeat.finishSeatReserve();
            validationResult.queue().finishQueue();
            validationResult.reservation().finishReserve();
            Payment payment = paymentRepository.findByReservationId(reservationId);
            payment.finishPayment();

            // 3. 히스토리 이벤트 생성 (아직 발행하지 않음)
            historyEvent = new PaymentHistoryInsertEvent(
                    validationResult.userId(),
                    concertSeat.getAmount(),
                    PaymentType.PAYMENT,
                    payment.getId()
            );

            // 4. 결제 완료 이벤트 발행
            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    validationResult.userId(),
                    payment.getId(),
                    payment.getPrice(),
                    PaymentType.PAYMENT
            ));

            // 5. 성공 시에만 히스토리 이벤트 발행
            eventPublisher.publishEvent(historyEvent);

            return new PaymentConcertResult(
                    concertSeat.getAmount(),
                    validationResult.reservation().getStatus(),
                    validationResult.queue().getStatus()
            );

        } catch (Exception e) {
            // 실패 시 보상 트랜잭션 이벤트 발행
            if (historyEvent != null) {
                eventPublisher.publishEvent(new PaymentHistoryCompensationEvent(historyEvent));
            }
            throw e;
        }
    }
}