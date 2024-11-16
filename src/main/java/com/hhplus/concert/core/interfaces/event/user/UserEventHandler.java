package com.hhplus.concert.core.interfaces.event.user;

import com.hhplus.concert.core.domain.event.user.UserChargeHistoryInsertEvent;
import com.hhplus.concert.core.domain.payment.PaymentHistory;
import com.hhplus.concert.core.domain.payment.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventHandler {
    private final PaymentHistoryRepository paymentHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePayHistoryCreated(UserChargeHistoryInsertEvent event) {
        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(
                event.userId(),
                event.amount(),
                event.paymentType()
        );
        paymentHistoryRepository.save(paymentHistory);
    }
}
