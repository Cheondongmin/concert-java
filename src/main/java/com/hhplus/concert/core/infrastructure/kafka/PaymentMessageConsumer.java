package com.hhplus.concert.core.infrastructure.kafka;

import com.hhplus.concert.core.domain.message.MessageSender;
import com.hhplus.concert.core.domain.payment.PaymentMessageSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMessageConsumer {
    private final MessageSender messageSender;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @KafkaListener(
            topics = "payment-notification",
            groupId = "PAYMENT-CONSUMER-GROUP",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Async
    public void handlePaymentNotification(PaymentMessageSendEvent event) throws Exception {
        try {
            String message = String.format("""
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
            );
            messageSender.sendMessage(message);
        } catch (Exception e) {
            log.error("텔레그램 알림 발송 실패: {}", event.mail(), e);
            throw e;
        }
    }
}