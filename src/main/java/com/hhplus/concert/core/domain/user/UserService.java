package com.hhplus.concert.core.domain.user;

import com.hhplus.concert.core.domain.payment.PaymentHistory;
import com.hhplus.concert.core.domain.payment.PaymentHistoryRepository;
import com.hhplus.concert.core.domain.payment.PaymentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional(readOnly = true)
    public long selectUserAmount(String token) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findById(userId);
        return user.getUserAmount();
    }

    @Transactional
    public Long chargeUserAmount(String token, Long amount) {
        long userId = Users.extractUserIdFromJwt(token);
        Users user = userRepository.findByIdWithLock(userId);
        user.addAmount(amount);
        PaymentHistory paymentHistory = PaymentHistory.enterPaymentHistory(user.getId(), amount, PaymentType.REFUND);
        paymentHistoryRepository.save(paymentHistory);
        return user.getUserAmount();
    }
}
