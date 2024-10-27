package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.IntegrationTest;
import com.hhplus.concert.core.domain.queue.Queue;
import com.hhplus.concert.core.domain.queue.QueueRepository;
import com.hhplus.concert.core.domain.queue.QueueStatus;
import com.hhplus.concert.core.domain.user.UserRepository;
import com.hhplus.concert.core.domain.user.Users;
import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class ConcertServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private ConcertScheduleRepository concertScheduleRepository;

    @Autowired
    private ConcertSeatRepository concertSeatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConcertService concertService;

    @Test
    void 콘서트_스케줄의_날짜의_좌석이_임시예약_혹은_예약된_상태인_경우_예외가_발생한다() {
        // given
        Users user = new Users(1L, 1000L); // 유저 잔액은 1000
        userRepository.save(user);
        String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
        Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
        queueRepository.save(queue);

        // 콘서트, 콘서트 스케줄 및 좌석 설정
        Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
        concertRepository.save(concert);
        ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDateTime.now().toLocalDate(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
        concertScheduleRepository.save(concertSchedule);
        ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.TEMP_RESERVED, null, LocalDateTime.now(), false);
        concertSeatRepository.save(concertSeat);

        // when & then
        assertThatThrownBy(() -> concertService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId()))
                .isInstanceOf(ApiException.class)
                .hasMessage("해당 좌석은 예약할 수 없는 상태 입니다.");
    }

    @Test
    void 콘서트가_만석일경우_예외가_발생한다() {
        // given
        Users user = new Users(1L, 1000L); // 유저 잔액은 1000
        userRepository.save(user);
        String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
        Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
        queueRepository.save(queue);

        // 콘서트, 콘서트 스케줄 및 좌석 설정
        Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
        concertRepository.save(concert);
        ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDateTime.now().toLocalDate(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.SOLD_OUT, LocalDateTime.now(), false);
        concertScheduleRepository.save(concertSchedule);
        ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
        concertSeatRepository.save(concertSeat);

        // when & then
        assertThatThrownBy(() -> concertService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId()))
                .isInstanceOf(ApiException.class)
                .hasMessage("죄송합니다. 해당 콘서트는 모든 좌석이 매진된 콘서트입니다.");
    }
}
