package com.hhplus.concert.core.domain.user.repository;

import com.hhplus.concert.core.domain.user.entlty.Users;

public interface UserRepository {
    void save(Long userId);

    Users findById(long userId);

    Users findByIdWithLock(long userId);
}
