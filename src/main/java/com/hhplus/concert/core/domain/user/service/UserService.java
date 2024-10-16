package com.hhplus.concert.core.domain.user.service;

import com.hhplus.concert.core.domain.user.entlty.Users;
import com.hhplus.concert.core.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
        user.chargeAmount(amount);
        return user.getUserAmount();
    }
}
