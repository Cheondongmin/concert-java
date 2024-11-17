package com.hhplus.concert.core.interfaces.event.payment;

import com.hhplus.concert.core.domain.payment.PaymentMessageSendEvent;
import com.hhplus.concert.core.domain.telegram.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final MessageSender messageSender;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 예약 완료 이벤트 수신 (텔레그램 메시지 전송)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCreated(PaymentMessageSendEvent event) {
        messageSender.sendMessage(
                String.format("""
                🎫 콘서트 결제가 완료되었습니다!
                예약자 ID: %s
                콘서트: %s
                시작 날짜: %s
                결제 날짜: %s
                결제 금액: %d원
                콘서트 시작 10분전에는 꼭 입장 부탁드립니다!!
                """,
                        event.mail(),
                        event.concertTitle(),
                        event.startDt().format(dateFormatter),
                        event.confirmDt().format(dateFormatter),
                        event.amount()
                )
        );
    }
}
