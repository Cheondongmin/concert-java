package com.hhplus.concert.app.infrastructure.repository.user.persistence;

import com.hhplus.concert.app.domain.user.entlty.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<Users, Long> {
}
