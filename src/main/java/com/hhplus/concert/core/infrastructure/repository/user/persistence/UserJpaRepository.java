package com.hhplus.concert.core.infrastructure.repository.user.persistence;

import com.hhplus.concert.core.domain.user.entlty.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<Users, Long> {
}
