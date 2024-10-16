package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.concert.ConcertRepository;
import com.hhplus.concert.core.infrastructure.repository.concert.persistence.ConcertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository jpaRepository;
}
