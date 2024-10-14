package com.hhplus.concert.app.domain.queue.entlty;

import com.hhplus.concert.app.domain.user.entlty.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Queue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QueueStatus status;

    @Column(name = "entered_dt", nullable = false)
    private LocalDateTime enteredDt;

    @Column(name = "expired_dt")
    private LocalDateTime expiredDt;
}

enum QueueStatus {
    WAITING,
    PROGRESS,
    DONE,
    EXPIRED
}
