package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final UserRepository userRepository;

    @CircuitBreaker(name = "concertService", fallbackMethod = "selectConcertListFallback")
    @Transactional(readOnly = true)
    public List<SelectConcertResult> selectConcertList(String token) {
        try {
            long userId = Users.extractUserIdFromJwt(token);
            userRepository.findById(userId);
            return concertScheduleRepository.findConcertSchedule();
        } catch (Exception e) {
            log.error("Error in selectConcertList: {}", e.getMessage(), e);
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private List<SelectConcertResult> selectConcertListFallback(String token, Exception e) {
        log.error("Circuit breaker fallback for concert list: {}", e.getMessage());
        return Collections.emptyList();  // 빈 리스트 반환
    }

    @CircuitBreaker(name = "concertService", fallbackMethod = "selectConcertSeatListFallback")
    @Transactional(readOnly = true)
    public List<SelectSeatResult> selectConcertSeatList(String token, long scheduleId) {
        try {
            long userId = Users.extractUserIdFromJwt(token);
            userRepository.findById(userId);
            return concertSeatRepository.findConcertSeat(scheduleId);
        } catch (Exception e) {
            log.error("Error in selectConcertSeatList: {}", e.getMessage(), e);
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private List<SelectSeatResult> selectConcertSeatListFallback(String token, long scheduleId, Exception e) {
        log.error("Circuit breaker fallback for seat list: {}", e.getMessage());
        return Collections.emptyList();  // 빈 리스트 반환
    }
}