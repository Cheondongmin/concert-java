package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.concert.ConcertSchedule;
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

    @Override
    public ConcertSchedule findById(long scheduleId) {
        return jpaRepository.findById(scheduleId).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디를 가진 콘서트 스케쥴이 존재하지 않습니다."));
    }

    @Override
    public void save(ConcertSchedule concertSchedule) {
        jpaRepository.save(concertSchedule);
    }
}
