package com.hhplus.concert.core.domain.payment;

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
@Table(name = "PAYMENT")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "price", nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "created_dt", nullable = false)
    private LocalDateTime createdDt;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = false;

    public Payment(long userId, long reservationId, long price, PaymentStatus status) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.price = price;
        this.status = status;
        this.createdDt = LocalDateTime.now();
        this.isDelete = false;
    }

    public static Payment enterPayment(long userId, long reservationId, long price, PaymentStatus status) {
        return new Payment(userId, reservationId, price,status);
    }

    public void finishPayment() {
        this.status = PaymentStatus.DONE;
    }
}