package com.hhplus.concert.core.domain.user;

public interface UserRepository {
    void save(Long userId);

    Users findById(long userId);

    Users findByIdWithLock(long userId);
}
