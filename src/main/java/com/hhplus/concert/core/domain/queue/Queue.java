package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.exception.ExceptionCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "QUEUE")
@Component
public class Queue {

    private static Key key;  // JWT 서명 키를 위한 정적 변수

    @Value("${jwt.secret-key}")
    private String secretKeyString;  // JWT 키를 가져오는 필드

    @PostConstruct
    public void initKey() {
        key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "token", nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QueueStatus status;

    @Column(name = "entered_dt", nullable = false)
    private LocalDateTime enteredDt;

    @Column(name = "expired_dt")
    private LocalDateTime expiredDt;

    public Queue(Long userId, String token) {
        this.userId = userId;
        this.token = token;
        this.status = QueueStatus.WAITING;
        this.enteredDt = LocalDateTime.now();
        this.expiredDt = LocalDateTime.now().plusMinutes(5);
    }

    public Queue(Long userId, String token, QueueStatus status, LocalDateTime expiredDt) {
        this.userId = userId;
        this.token = token;
        this.status = status;
        this.enteredDt = LocalDateTime.now();
        this.expiredDt = expiredDt;
    }

    public Queue(Long userId, String token, QueueStatus status, LocalDateTime enteredDt, LocalDateTime expiredDt) {
        this.userId = userId;
        this.token = token;
        this.status = status;
        this.enteredDt = enteredDt;
        this.expiredDt = expiredDt;
    }

    // 토큰 유효성 검증 로직
    public boolean isTokenValid() {
        if (status == QueueStatus.WAITING || status == QueueStatus.PROGRESS) {
            return expiredDt == null || expiredDt.isAfter(LocalDateTime.now());
        }
        return false;
    }

    public void tokenReserveCheck() {
        if(this.status != QueueStatus.PROGRESS) {
            throw new ApiException(ExceptionCode.E001, LogLevel.ERROR);
        }
    }

    // 새로운 토큰을 발급하거나 기존 토큰을 반환하는 로직
    public static Queue enterQueue(Queue existingQueue, Long userId) {
        if (existingQueue != null && existingQueue.isTokenValid()) {
            return existingQueue;
        }
        return new Queue(userId, generateJwtToken(userId)); // 새 토큰 발급
    }

    // JWT 토큰 생성 (서명 포함)
    public static String generateJwtToken(Long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("token", UUID.randomUUID().toString())
                .claim("enteredDt", new Date())
                .claim("expiredDt", new Date(System.currentTimeMillis() + 300000)) // 5분 후 만료
                .signWith(key, SignatureAlgorithm.HS256)  // 서명 포함
                .compact();
    }

    public void checkWaitingQueue(List<Queue> queueList) {
        if(queueList.size() <= 30 && this.getStatus() == QueueStatus.WAITING) {
            LocalDateTime expiredDt = LocalDateTime.now().plusMinutes(10);
            this.status = QueueStatus.PROGRESS;
            this.enteredDt  = expiredDt;
        } else {
            if(this.getStatus() == QueueStatus.EXPIRED) {
                throw new ApiException(ExceptionCode.E003, LogLevel.ERROR);
            }
        }
    }

    public void finishQueue() {
        this.status = QueueStatus.DONE;
    }

    public static void tokenNullCheck(String token) {
        if(!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
        }
    }

    public void checkToken() {
        if(status != QueueStatus.PROGRESS) {
            throw new ApiException(ExceptionCode.E403, LogLevel.WARN);
        }
        if(!this.expiredDt.isAfter(LocalDateTime.now())) {
            throw new ApiException(ExceptionCode.E403, LogLevel.WARN);
        }
    }

    public void statusChange(QueueStatus newStatus) {
        this.status = newStatus;
    }
}
