package com.hhplus.concert.core.interfaces.event.payment;

import com.hhplus.concert.core.domain.event.payment.PaymentHistoryCompensationEvent;
import com.hhplus.concert.core.domain.event.payment.PaymentHistoryInsertEvent;
import com.hhplus.concert.core.domain.event.reservation.ReservationCreatedEvent;
import com.hhplus.concert.core.domain.payment.PaymentHistory;
import com.hhplus.concert.core.domain.payment.PaymentHistoryRepository;
import com.hhplus.concert.core.domain.telegram.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class PaymentEventHandler {
    private final MessageSender messageSender;
    private final PaymentHistoryRepository paymentHistoryRepository;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 예약 완료 이벤트 수신 (텔레그램 메시지 전송)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCreated(ReservationCreatedEvent event) {
        messageSender.sendMessage(
                String.format("""
                🎫 콘서트 예약이 완료되었습니다!
                예약 ID: %d
                좌석 번호: %d
                좌석 가격: %d원
                시작 시간: %s
                5분 이내에 결제를 진행해주세요.
                """,
                        event.reservationId(),
                        event.seatId(),
                        event.amount(),
                        event.startDt().format(dateFormatter)
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePayHistoryCreated(PaymentHistoryInsertEvent event) {
        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(
                event.userId(),
                event.amount(),
                event.paymentType(),
                event.paymentId()
        );
        paymentHistoryRepository.save(paymentHistory);
    }

    // 히스토리 저장 실패시 삭제처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handlePaymentHistoryCompensation(PaymentHistoryCompensationEvent event) {
        PaymentHistory failedPayment = paymentHistoryRepository.findByPaymentId(event.historyInsertEvent().paymentId());

        if (failedPayment != null) {
            failedPayment.isDelete(true);
            paymentHistoryRepository.save(failedPayment);
        }
    }
}
