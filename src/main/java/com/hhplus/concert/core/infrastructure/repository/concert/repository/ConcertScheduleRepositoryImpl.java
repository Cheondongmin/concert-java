package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.concert.ConcertScheduleRepository;
import com.hhplus.concert.core.domain.concert.SelectConcertResult;
import com.hhplus.concert.core.infrastructure.repository.concert.persistence.ConcertScheduleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConcertScheduleRepositoryImpl implements ConcertScheduleRepository {
    private final ConcertScheduleJpaRepository jpaRepository;

    @Override
    public List<SelectConcertResult> findConcertSchedule() {
        return jpaRepository.findConcertSchedule();
    }
}
