package com.hhplus.concert.core.domain.user;

public interface UserRepository {
    void save(Users user);

    Users findById(long userId);

    Users findByIdWithLock(long userId);
}
