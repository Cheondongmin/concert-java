package com.hhplus.concert.core.domain.user;

import com.hhplus.concert.core.interfaces.api.exception.ApiException;
import com.hhplus.concert.core.interfaces.api.exception.ExceptionCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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

import java.security.Key;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "USERS")
@Component
public class Users {

    @Transient
    private static Key secretKey;  // JWT 서명 키를 위한 정적 변수

    @Value("${jwt.secret-key}")
    @Transient
    private String secretKeyString;  // JWT 키를 주입받기 위한 필드

    @PostConstruct
    public void initKey() {
        secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_mail", nullable = false)
    private String userMail;

    @Column(name = "user_amount", nullable = false)
    private Long userAmount;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    @Version  // 낙관적 락을 위한 버전 필드
    private int version;

    public Users(Long userId, Long userAmount) {
        String randomEmail = generateRandomEmail();
        this.id = userId;
        this.userMail = randomEmail;
        this.userAmount = userAmount;
        this.createdDt = LocalDateTime.now();
        this.isDelete = false;
    }

    // 랜덤 이메일 생성 메서드
    private static String generateRandomEmail() {
        String uuid = UUID.randomUUID().toString();
        return uuid + "@gmail.com";
    }

    // JWT 토큰에서 userId 추출
    public static Long extractUserIdFromJwt(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)  // 주입된 키 사용
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    public void addAmount(Long amount) {
        if (0 >= amount) {
            throw new IllegalArgumentException("충전금액을 0 이상으로 설정해주세요.");
        }
        this.userAmount += amount;
    }

    public void checkConcertAmount(Long seatAmount) {
        if (this.userAmount < seatAmount) {
            throw new ApiException(ExceptionCode.E005, LogLevel.INFO);
        }
        this.userAmount -= seatAmount;
    }
}
