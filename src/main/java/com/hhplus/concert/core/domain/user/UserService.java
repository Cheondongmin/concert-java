package com.hhplus.concert.core.domain.user;

import com.hhplus.concert.core.domain.payment.PaymentHistory;
import com.hhplus.concert.core.domain.payment.PaymentHistoryRepository;
import com.hhplus.concert.core.domain.payment.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRedisRock userRedisRock;

    @Transactional(readOnly = true)
    public long selectUserAmount(String token) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);
        return user.getUserAmount();
    }

    public Long chargeUserAmountRedis(String token, Long amount) {
        String lockKey = "lock:토큰:" + token;
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return userRedisRock.executeWithLock(
                lockKey,
                10,  // waitTime
                15,  // leaseTime
                () -> transactionTemplate.execute(status -> {
                    try {
                        long userId = Users.extractUserIdFromJwt(token);
                        Users user = userRepository.findById(userId);
                        user.addAmount(amount);
                        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(user.getId(), amount, PaymentType.REFUND);
                        paymentHistoryRepository.save(paymentHistory);
                        return user.getUserAmount();
                    } catch (Exception e) {
                        status.setRollbackOnly();
                        throw e;
                    }
                })
        );
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
        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(user.getId(), amount, PaymentType.REFUND);
        paymentHistoryRepository.save(paymentHistory);
        return user.getUserAmount();
    }
}
