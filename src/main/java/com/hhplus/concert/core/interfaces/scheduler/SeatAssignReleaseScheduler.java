package com.hhplus.concert.core.interfaces.scheduler;

import com.hhplus.concert.core.domain.concert.SeatAssignReleaseChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatAssignReleaseScheduler {
    private final SeatAssignReleaseChecker seatAssignReleaseChecker;

    /**
     * 작업을 마친 시점 기준으로 5초마다 임시 예약 직후 5분 인에 결제가 완료되지 않을 경우 임시 예약을 삭제한다.
     */
    @Scheduled(fixedDelay = 5000)
    public void checkSeatAssignRelease() {
        seatAssignReleaseChecker.release();
    }
}
