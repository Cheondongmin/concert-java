package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.logging.LogLevel;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "CONCERT_SEAT")
public class ConcertSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_schedule_id", nullable = false)
    private Long concertScheduleId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_status", nullable = false)
    private SeatStatus seatStatus;

    @Column(name = "reserved_until_dt")
    private LocalDateTime reservedUntilDt;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    @Version  // 낙관적 락을 위한 버전 필드
    private int version;

    public ConcertSeat(Long id, Long concertScheduleId, Long amount, Integer position, SeatStatus seatStatus, LocalDateTime reservedUntilDt, LocalDateTime createdDt, Boolean isDelete) {
        this.id = id;
        this.concertScheduleId = concertScheduleId;
        this.amount = amount;
        this.position = position;
        this.seatStatus = seatStatus;
        this.reservedUntilDt = reservedUntilDt;
        this.createdDt = createdDt;
        this.isDelete = isDelete;
    }

    public void isReserveCheck() {
        if(this.seatStatus != SeatStatus.AVAILABLE) {
            throw new ApiException(ExceptionCode.E004, LogLevel.ERROR);
        } else {
            this.seatStatus = SeatStatus.TEMP_RESERVED;
        }
    }

    public void finishSeatReserve() {
        this.seatStatus = SeatStatus.RESERVED;
    }
}
