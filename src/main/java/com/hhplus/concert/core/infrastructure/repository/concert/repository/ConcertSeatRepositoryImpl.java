package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.concert.ConcertSeat;
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

    @Override
    public ConcertSeat findByIdWithLock(long seatId) {
        return jpaRepository.findByIdWithLock(seatId).orElseThrow(
                () -> new RuntimeException("해당 정보를 가진 좌석을 조회할 수 없습니다."));
    }

    @Override
    public ConcertSeat findById(long seatId) {
        return jpaRepository.findById(seatId).orElseThrow(
                () -> new RuntimeException("해당 정보를 가진 좌석을 조회할 수 없습니다."));
    }

    @Override
    public void save(ConcertSeat concertSeat) {
        jpaRepository.save(concertSeat);
    }

    @Override
    public ConcertSeat findAvailableSeatWithLock(long seatId) {
        return (ConcertSeat) jpaRepository.findAvailableSeatWithLock(seatId).orElseThrow(
                () -> new RuntimeException("해당 정보를 가진 좌석을 조회할 수 없습니다."));
    }
}
