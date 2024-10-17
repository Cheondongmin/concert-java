package com.hhplus.concert.core.domain.concert;

public interface ConcertRepository {
    Concert findById(Long concertId);
    void save(Concert concert);
}
