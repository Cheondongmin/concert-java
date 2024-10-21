package com.hhplus.concert.core.domain.concert;

import com.hhplus.concert.core.domain.user.Users;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "RESERVATION")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    @Column(name = "concert_title", nullable = false)
    private String concertTitle;

    @Column(name = "concert_open_dt", nullable = false)
    private LocalDate concertOpenDt;

    @Column(name = "concert_start_dt", nullable = false)
    private LocalDateTime concertStartDt;

    @Column(name = "concert_end_dt", nullable = false)
    private LocalDateTime concertEndDt;

    @Column(name = "seat_amount", nullable = false)
    private Long seatAmount;

    @Column(name = "seat_position", nullable = false)
    private Integer seatPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @Column(name = "reserved_dt", nullable = false)
    private LocalDateTime reservedDt;

    @Column(name = "reserved_until_dt")
    private LocalDateTime reservedUntilDt;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    public Reservation(Long userId, Long seatId, String concertTitle, LocalDate concertOpenDt, LocalDateTime concertStartDt, LocalDateTime concertEndDt, Long seatAmount, Integer seatPosition) {
        this.userId = userId;
        this.seatId = seatId;
        this.concertTitle = concertTitle;
        this.concertOpenDt = concertOpenDt;
        this.concertStartDt = concertStartDt;
        this.concertEndDt = concertEndDt;
        this.seatAmount = seatAmount;
        this.seatPosition = seatPosition;
        this.status = ReservationStatus.TEMP_RESERVED;
        this.reservedDt = LocalDateTime.now();
        this.reservedUntilDt = LocalDateTime.now().plusMinutes(5);
        this.createdDt = LocalDateTime.now();
        this.isDelete = false;
    }

    public static Reservation enterReservation(Users user, Concert concert, ConcertSeat concertSeat, ConcertSchedule concertSchedule) {
        return new Reservation(user.getId(), concertSeat.getId(), concert.getTitle(), concertSchedule.getOpenDt(), concertSchedule.getStartDt(), concertSchedule.getEndDt(), concertSeat.getAmount(), concertSeat.getPosition());
    }

    public void finishReserve() {
        if(this.status == ReservationStatus.TEMP_RESERVED) {
            this.status = ReservationStatus.RESERVED;
        }
    }
}
