package com.hhplus.concert.core.infrastructure.repository.user.persistence;

import com.hhplus.concert.core.domain.user.Users;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<Users, Long> {
    @Query("select u from Users u where u.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Users> findByIdWithLock(@Param("id") long userId);
}
