package com.hhplus.concert.core.infrastructure.repository.user.repository;

import com.hhplus.concert.core.domain.user.entlty.Users;
import com.hhplus.concert.core.domain.user.repository.UserRepository;
import com.hhplus.concert.core.infrastructure.repository.user.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(Long userId) {
        userJpaRepository.save(new Users(userId));
    }

    @Override
    public Users findById(long userId) {
        return userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 유저가 존재하지 않습니다."));
    }

    @Override
    public Users findByIdWithLock(long userId) {
        return userJpaRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 유저가 존재하지 않습니다."));
    }
}
