package com.hhplus.concert.core.interfaces.event.reservation;

import com.hhplus.concert.core.domain.reservation.ReservationMessageSendEvent;
import com.hhplus.concert.core.domain.message.MessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class ReservationEventListener {
    private final MessageSender messageSender;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 예약 완료 이벤트 수신 (텔레그램 메시지 전송)
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationCreated(ReservationMessageSendEvent event) {
        messageSender.sendMessage(
                String.format("""
                🎫 콘서트 예약이 완료되었습니다!
                예약자 ID: %s
                콘서트: %s
                시작 날짜: %s
                예약 날짜: %s
                만료 날짜: %s
                결제 금액: %d원
                5분 이내에 결제를 진행해주세요.
                """,
                        event.mail(),
                        event.concertTitle(),
                        event.startDt().format(dateFormatter),
                        event.confirmDt().format(dateFormatter),
                        event.untilDt().format(dateFormatter),
                        event.amount()
                )
        );
    }
}
