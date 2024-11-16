package com.hhplus.concert.core.domain.user;

import com.hhplus.concert.core.domain.event.user.UserChargeHistoryInsertEvent;
import com.hhplus.concert.core.domain.payment.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public long selectUserAmount(String token) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);
        return user.getUserAmount();
    }

    @Transactional
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 10,
            backoff = @Backoff(delay = 200)
    )
    public Long chargeUserAmount(String token, Long amount) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);
        user.addAmount(amount);

        // 히스토리 이벤트 생성 (아직 발행하지 않음)
        UserChargeHistoryInsertEvent historyEvent = new UserChargeHistoryInsertEvent(
                user.getId(),
                amount,
                PaymentType.REFUND
        );

        eventPublisher.publishEvent(historyEvent);

        return user.getUserAmount();
    }
}
