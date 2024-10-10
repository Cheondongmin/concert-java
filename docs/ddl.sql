-- USER 테이블: 유저 정보를 저장하는 테이블
CREATE TABLE USER
(
    id          BIGINT PRIMARY KEY COMMENT '유저 ID (PK)',
    user_mail   VARCHAR(255) NOT NULL COMMENT '유저 메일',
    user_amount BIGINT       NOT NULL COMMENT '잔액',
    created_dt  DATETIME     NOT NULL COMMENT '생성 시간',
    is_delete   BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (Y, N)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- QUEUE 테이블: 유저의 대기열 정보를 저장하는 테이블
CREATE TABLE QUEUE
(
    id         BIGINT PRIMARY KEY COMMENT '대기 번호 (PK)',
    user_id    BIGINT       NOT NULL COMMENT '유저 ID (FK)',
    token      VARCHAR(255) NOT NULL COMMENT '대기열 토큰',
    status     ENUM('WAITING', 'PROGRESS', 'DONE', 'EXPIRED') NOT NULL COMMENT '대기열 상태',
    entered_dt DATETIME     NOT NULL COMMENT '대기열 진입 시간',
    expired_dt DATETIME DEFAULT NULL COMMENT '대기열 만료 시간',
    CONSTRAINT fk_queue_user FOREIGN KEY (user_id) REFERENCES USER (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CONCERT 테이블: 콘서트 정보를 저장하는 테이블
CREATE TABLE CONCERT
(
    id         BIGINT PRIMARY KEY COMMENT '콘서트 ID (PK)',
    title      VARCHAR(255) NOT NULL COMMENT '콘서트 제목',
    created_dt DATETIME     NOT NULL COMMENT '생성 시간',
    is_delete  BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (Y, N)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CONCERT_SCHEDULE 테이블: 콘서트 일정을 저장하는 테이블
CREATE TABLE CONCERT_SCHEDULE
(
    id                BIGINT PRIMARY KEY COMMENT '콘서트 일정 ID (PK)',
    concert_id        BIGINT   NOT NULL COMMENT '콘서트 ID (FK)',
    open_dt           DATE     NOT NULL COMMENT '콘서트 개최 날짜',
    start_dt          DATETIME NOT NULL COMMENT '콘서트 시작 시간',
    end_dt            DATETIME NOT NULL COMMENT '콘서트 종료 시간',
    total_seat        INT      NOT NULL COMMENT '전체 좌석 수',
    reservation_seat  INT      NOT NULL COMMENT '남은 좌석 수',
    total_seat_status ENUM('SOLD_OUT', 'AVAILABLE') NOT NULL COMMENT '전체 좌석 상태',
    created_dt        DATETIME NOT NULL COMMENT '생성 시간',
    is_delete         BOOLEAN  NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (Y, N)',
    CONSTRAINT fk_concert_schedule_concert FOREIGN KEY (concert_id) REFERENCES CONCERT (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CONCERT_SEAT 테이블: 각 콘서트 일정의 좌석 정보를 저장하는 테이블
CREATE TABLE CONCERT_SEAT
(
    id                  BIGINT PRIMARY KEY COMMENT '좌석 ID (PK)',
    concert_schedule_id BIGINT   NOT NULL COMMENT '콘서트 일정 ID (FK)',
    amount              INT      NOT NULL COMMENT '좌석 금액',
    position            INT      NOT NULL COMMENT '좌석 번호',
    seat_status         ENUM('AVAILABLE', 'TEMP_RESERVED', 'RESERVED') NOT NULL COMMENT '좌석 상태',
    reserved_until_dt   DATETIME          DEFAULT NULL COMMENT '임시 예약 만료 시간',
    created_dt          DATETIME NOT NULL COMMENT '생성 시간',
    is_delete           BOOLEAN  NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (Y, N)',
    CONSTRAINT fk_concert_seat_schedule FOREIGN KEY (concert_schedule_id) REFERENCES CONCERT_SCHEDULE (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- RESERVATION 테이블: 예약 정보를 저장하는 테이블
CREATE TABLE RESERVATION
(
    id                  BIGINT PRIMARY KEY COMMENT '예약 ID (PK)',
    user_id             BIGINT       NOT NULL COMMENT '유저 ID (FK)',
    concert_schedule_id BIGINT       NOT NULL COMMENT '콘서트 일정 ID (FK)',
    seat_id             BIGINT       NOT NULL COMMENT '좌석 ID (FK)',
    concert_title       VARCHAR(255) NOT NULL COMMENT '콘서트 제목',
    concert_open_dt     DATE         NOT NULL COMMENT '콘서트 개최 날짜',
    concert_start_dt    DATETIME     NOT NULL COMMENT '콘서트 시작 시간',
    concert_end_dt      DATETIME     NOT NULL COMMENT '콘서트 종료 시간',
    seat_amount         BIGINT       NOT NULL COMMENT '좌석 금액',
    seat_position       INT          NOT NULL COMMENT '좌석 번호',
    status              ENUM('TEMP_RESERVED', 'RESERVED', 'CANCELED') NOT NULL COMMENT '예약 상태',
    reserved_dt         DATETIME     NOT NULL COMMENT '예약 시간',
    reserved_until_dt   DATETIME              DEFAULT NULL COMMENT '예약 만료 시간',
    created_dt          DATETIME     NOT NULL COMMENT '생성 시간',
    is_delete           BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (Y, N)',
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES USER (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_reservation_schedule FOREIGN KEY (concert_schedule_id) REFERENCES CONCERT_SCHEDULE (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES CONCERT_SEAT (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PAYMENT 테이블: 결제 정보를 저장하는 테이블
CREATE TABLE PAYMENT
(
    id             BIGINT PRIMARY KEY COMMENT '결제 번호 (PK)',
    user_id        BIGINT   NOT NULL COMMENT '유저 ID (FK)',
    reservation_id BIGINT   NOT NULL COMMENT '예약 ID (FK)',
    price          BIGINT   NOT NULL COMMENT '결제 금액',
    status         ENUM('PROGRESS', 'DONE', 'CANCELED') NOT NULL COMMENT '결제 상태',
    created_dt     DATETIME NOT NULL COMMENT '결제 시간',
    is_delete      BOOLEAN  NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (Y, N)',
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES USER (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES RESERVATION (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PAYMENT_HISTORY 테이블: 유저 금액 사용 내역을 저장하는 테이블
CREATE TABLE PAYMENT_HISTORY
(
    id            BIGINT PRIMARY KEY COMMENT '금액 사용 내역 ID (PK)',
    user_id       BIGINT   NOT NULL COMMENT '유저 ID (FK)',
    payment_id    BIGINT   NOT NULL COMMENT '결제 ID (FK)',
    amount_change INT      NOT NULL COMMENT '금액 변경',
    type          ENUM('PAYMENT', 'REFUND') NOT NULL COMMENT '금액 사용 타입',
    created_dt    DATETIME NOT NULL COMMENT '금액 변경 시간',
    is_delete     BOOLEAN  NOT NULL DEFAULT FALSE COMMENT '삭제 여부 (Y, N)',
    CONSTRAINT fk_payment_history_user FOREIGN KEY (user_id) REFERENCES USER (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_payment_history_payment FOREIGN KEY (payment_id) REFERENCES PAYMENT (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
