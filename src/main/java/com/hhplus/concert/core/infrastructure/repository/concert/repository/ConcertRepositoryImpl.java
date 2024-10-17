package com.hhplus.concert.core.infrastructure.repository.concert.repository;

import com.hhplus.concert.core.domain.concert.Concert;
import com.hhplus.concert.core.domain.concert.ConcertRepository;
import com.hhplus.concert.core.infrastructure.repository.concert.persistence.ConcertJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    private final ConcertJpaRepository jpaRepository;

    @Override
    public Concert findById(Long concertId) {
        return jpaRepository.findById(concertId).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디를 가진 콘서트가 존재하지 않습니다."));
    }

    @Override
    public void save(Concert concert) {
        jpaRepository.save(concert);
    }
}
