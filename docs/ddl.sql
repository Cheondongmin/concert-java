-- USER 테이블: 유저 정보를 저장하는 테이블
CREATE TABLE USER
(
    id          BIGINT PRIMARY KEY,    -- 유저 ID (PK)
    user_mail   VARCHAR(255) NOT NULL, -- 유저 이메일
    user_amount INT          NOT NULL  -- 유저 잔액
);

-- USER_QUEUE 테이블: 유저의 대기열 정보를 저장하는 테이블
CREATE TABLE QUEUE
(
    id         BIGINT PRIMARY KEY,                                           -- 대기열 번호 (PK)
    user_id    BIGINT       NOT NULL,                                        -- 유저 ID (FK)
    token      VARCHAR(255) NOT NULL,                                        -- 대기열 토큰
    status     ENUM('WAITING', 'PROGRESS', 'DONE', 'EXPIRED') NOT NULL,      -- 대기열 상태
    entered_dt DATETIME     NOT NULL,                                        -- 대기열 진입 시간
    expired_dt DATETIME,                                                     -- 대기열 만료 시간
    CONSTRAINT fk_user_queue_user FOREIGN KEY (user_id) REFERENCES USER (id) -- USER 테이블과 FK로 연결
);

-- CONCERT 테이블: 콘서트 정보를 저장하는 테이블
CREATE TABLE CONCERT
(
    id    BIGINT PRIMARY KEY,   -- 콘서트 ID (PK)
    title VARCHAR(255) NOT NULL -- 콘서트 제목
);

-- CONCERT_SCHEDULE 테이블: 콘서트 일정을 저장하는 테이블
CREATE TABLE CONCERT_SCHEDULE
(
    id                BIGINT PRIMARY KEY,                                                   -- 콘서트 일정 ID (PK)
    concert_id        BIGINT   NOT NULL,                                                    -- 콘서트 ID (FK)
    open_dt           DATE     NOT NULL,                                                    -- 콘서트 개최 날짜
    start_dt          DATETIME NOT NULL,                                                    -- 콘서트 시작 시간
    end_dt            DATETIME NOT NULL,                                                    -- 콘서트 종료 시간
    total_seat        INT      NOT NULL,                                                    -- 전체 좌석 수
    reservation_seat  INT      NOT NULL,                                                    -- 남은 좌석 수
    total_seat_status ENUM('SOLD_OUT', 'AVAILABLE') NOT NULL,                               -- 좌석 상태
    CONSTRAINT fk_concert_schedule_concert FOREIGN KEY (concert_id) REFERENCES CONCERT (id) -- CONCERT 테이블과 FK로 연결
);


-- CONCERT_SEAT 테이블: 각 콘서트 일정의 좌석 정보를 저장하는 테이블
CREATE TABLE CONCERT_SEAT
(
    id                  BIGINT PRIMARY KEY,                                                                -- 좌석 ID (PK)
    concert_schedule_id BIGINT NOT NULL,                                                                   -- 콘서트 일정 ID (FK)
    amount              INT    NOT NULL,                                                                   -- 좌석 금액
    position            INT    NOT NULL,                                                                   -- 좌석 번호
    seat_status         ENUM('AVAILABLE', 'TEMP_RESERVED', 'RESERVED') NOT NULL,                           -- 좌석 상태
    reserved_until      DATETIME,                                                                          -- 임시 예약 만료 시간
    CONSTRAINT fk_concert_seat_schedule FOREIGN KEY (concert_schedule_id) REFERENCES CONCERT_SCHEDULE (id) -- CONCERT_SCHEDULE 테이블과 FK로 연결
);

-- RESERVATION 테이블: 예약 정보를 저장하는 테이블
CREATE TABLE RESERVATION
(
    id                  BIGINT PRIMARY KEY,                                                                -- 예약 ID (PK)
    user_id             BIGINT       NOT NULL,                                                             -- 유저 ID (FK)
    concert_schedule_id BIGINT       NOT NULL,                                                             -- 콘서트 일정 ID (FK)
    seat_id             BIGINT       NOT NULL,                                                             -- 좌석 ID (FK)
    concert_title       VARCHAR(255) NOT NULL,                                                             -- 콘서트 제목
    concert_open_dt     DATE         NOT NULL,                                                             -- 콘서트 개최 날짜
    concert_start_dt    DATETIME     NOT NULL,                                                             -- 콘서트 시작 시간
    concert_end_dt      DATETIME     NOT NULL,                                                             -- 콘서트 종료 시간
    seat_amount         INT          NOT NULL,                                                             -- 좌석 금액
    seat_position       INT          NOT NULL,                                                             -- 좌석 번호
    status              ENUM('TEMP_RESERVED', 'RESERVED', 'CANCELED') NOT NULL,                            -- 예약 상태
    reserved_dt         DATETIME     NOT NULL,                                                             -- 예약 시간
    reserved_until_dt   DATETIME,                                                                          -- 예약 만료 시간
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES USER (id),                             -- USER 테이블과 FK로 연결
    CONSTRAINT fk_reservation_schedule FOREIGN KEY (concert_schedule_id) REFERENCES CONCERT_SCHEDULE (id), -- CONCERT_SCHEDULE 테이블과 FK로 연결
    CONSTRAINT fk_reservation_seat FOREIGN KEY (seat_id) REFERENCES CONCERT_SEAT (id)                      -- CONCERT_SEAT 테이블과 FK로 연결
);


-- PAYMENT 테이블: 결제 정보를 저장하는 테이블
CREATE TABLE PAYMENT
(
    id             BIGINT PRIMARY KEY,                                                         -- 결제 번호 (PK)
    user_id        BIGINT   NOT NULL,                                                          -- 유저 ID (FK)
    reservation_id BIGINT   NOT NULL,                                                          -- 예약 ID (FK)
    price          INT      NOT NULL,                                                          -- 결제 금액
    status         ENUM('PROGRESS', 'DONE', 'CANCELED') NOT NULL,                              -- 결제 상태
    created_dt     DATETIME NOT NULL,                                                          -- 결제 시간
    CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES USER (id),                     -- USER 테이블과 FK로 연결
    CONSTRAINT fk_payment_reservation FOREIGN KEY (reservation_id) REFERENCES RESERVATION (id) -- RESERVATION 테이블과 FK로 연결
);


-- USER_PAYMENT_HISTORY 테이블: 유저 금액 사용 내역을 저장하는 테이블
CREATE TABLE PAYMENT_HISTORY
(
    id            BIGINT PRIMARY KEY,                                                     -- 금액 사용 내역 ID (PK)
    user_id       BIGINT   NOT NULL,                                                      -- 유저 ID (FK)
    payment_id    BIGINT   NOT NULL,                                                      -- 결제 ID (FK)
    amount_change INT      NOT NULL,                                                      -- 금액 변경
    type          ENUM('PAYMENT', 'REFUND') NOT NULL,                                     -- 금액 사용 타입
    change_dt     DATETIME NOT NULL,                                                      -- 금액 변경 시간
    CONSTRAINT fk_amount_history_user FOREIGN KEY (user_id) REFERENCES USER (id),         -- USER 테이블과 FK로 연결
    CONSTRAINT fk_amount_history_payment FOREIGN KEY (payment_id) REFERENCES PAYMENT (id) -- PAYMENT 테이블과 FK로 연결
);

