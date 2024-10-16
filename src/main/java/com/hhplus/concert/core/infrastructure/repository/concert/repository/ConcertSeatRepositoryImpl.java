package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.concert.ConcertSeatRepository;
import com.hhplus.concert.core.domain.concert.SelectSeatResult;
import com.hhplus.concert.core.infrastructure.repository.concert.persistence.ConcertSeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertSeatRepositoryImpl implements ConcertSeatRepository {

    private final ConcertSeatJpaRepository jpaRepository;

    @Override
    public List<SelectSeatResult> findConcertSeat(long scheduleId) {
        return jpaRepository.findConcertSeat(scheduleId);
    }
}
