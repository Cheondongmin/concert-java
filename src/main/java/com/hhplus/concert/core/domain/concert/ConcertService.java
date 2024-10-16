package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertService {

    private final ConcertScheduleRepository concertScheduleRepository;
    private final ConcertSeatRepository concertSeatRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SelectConcertResult> selectConcertList(String token) {
        long userId = Users.extractUserIdFromJwt(token);
        userRepository.findById(userId);
        return concertScheduleRepository.findConcertSchedule();
    }

    @Transactional(readOnly = true)
    public List<SelectSeatResult> selectConcertSeatList(String token, long scheduleId) {
        long userId = Users.extractUserIdFromJwt(token);
        userRepository.findById(userId);
        return concertSeatRepository.findConcertSeat(scheduleId);
    }
}
