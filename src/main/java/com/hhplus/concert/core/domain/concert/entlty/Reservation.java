package com.hhplus.concert.core.domain.concert.entlty;

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
}
