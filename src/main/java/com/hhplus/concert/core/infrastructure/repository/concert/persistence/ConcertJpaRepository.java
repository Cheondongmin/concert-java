package com.hhplus.concert.core.infrastructure.repository.concert.persistence;

import com.hhplus.concert.core.domain.concert.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {
}
