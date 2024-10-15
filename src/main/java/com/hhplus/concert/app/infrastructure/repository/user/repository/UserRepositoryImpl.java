package com.hhplus.concert.app.infrastructure.repository.user.repository;

import com.hhplus.concert.app.domain.user.entlty.Users;
import com.hhplus.concert.app.domain.user.repository.UserRepository;
import com.hhplus.concert.app.infrastructure.repository.user.persistence.UserJpaRepository;
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
}
