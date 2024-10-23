package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.interfaces.api.support.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.support.exception.ExceptionCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.logging.LogLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "CONCERT_SCHEDULE")
public class ConcertSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "concert_id", nullable = false)
    private Long concertId;

    @Column(name = "open_dt", nullable = false)
    private LocalDate openDt;

    @Column(name = "start_dt", nullable = false)
    private LocalDateTime startDt;

    @Column(name = "end_dt", nullable = false)
    private LocalDateTime endDt;

    @Column(name = "total_seat", nullable = false)
    private Integer totalSeat;

    @Column(name = "reservation_seat", nullable = false)
    private Integer reservationSeat;

    @Enumerated(EnumType.STRING)
    @Column(name = "total_seat_status", nullable = false)
    private TotalSeatStatus totalSeatStatus;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    public void isSoldOutCheck() {
        if(this.totalSeatStatus != TotalSeatStatus.AVAILABLE) {
            throw new ApiException(ExceptionCode.E002, LogLevel.INFO);
        }
    }
}
