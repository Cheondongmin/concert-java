create table CONCERT
(
    id         bigint auto_increment comment '콘서트 ID (PK)'
        primary key,
    title      varchar(255) not null comment '콘서트 제목',
    created_dt datetime     not null comment '생성 시간',
    is_delete  tinyint(1) default 0 not null comment '삭제 여부 (Y, N)'
);

create table CONCERT_SCHEDULE
(
    id                bigint auto_increment comment '콘서트 일정 ID (PK)'
        primary key,
    concert_id        bigint   not null comment '콘서트 ID (FK)',
    open_dt           date     not null comment '콘서트 개최 날짜',
    start_dt          datetime not null comment '콘서트 시작 시간',
    end_dt            datetime not null comment '콘서트 종료 시간',
    total_seat        int      not null comment '전체 좌석 수',
    reservation_seat  int      not null comment '남은 좌석 수',
    total_seat_status enum ('SOLD_OUT', 'AVAILABLE') not null comment '전체 좌석 상태',
    created_dt        datetime not null comment '생성 시간',
    is_delete         tinyint(1) default 0           not null comment '삭제 여부 (Y, N)',
    constraint fk_concert_schedule_concert
        foreign key (concert_id) references CONCERT (id)
            on update cascade on delete cascade
);

create table CONCERT_SEAT
(
    id                  bigint auto_increment comment '좌석 ID (PK)'
        primary key,
    concert_schedule_id bigint   not null comment '콘서트 일정 ID (FK)',
    amount              int      not null comment '좌석 금액',
    position            int      not null comment '좌석 번호',
    seat_status         enum ('AVAILABLE', 'TEMP_RESERVED', 'RESERVED') not null comment '좌석 상태',
    reserved_until_dt   datetime null comment '임시 예약 만료 시간',
    created_dt          datetime not null comment '생성 시간',
    is_delete           tinyint(1) default 0                            not null comment '삭제 여부 (Y, N)',
    version             int default 0                            not null comment '낙관적 락 사용하기 위한 버전 컬럼',
    constraint fk_concert_seat_schedule
        foreign key (concert_schedule_id) references CONCERT_SCHEDULE (id)
            on update cascade on delete cascade
);

create table USERS
(
    id          bigint auto_increment comment '유저 ID (PK)'
        primary key,
    user_mail   varchar(255)                         not null comment '유저 메일',
    user_amount bigint                               not null comment '잔액',
    created_dt  datetime default current_timestamp() not null comment '생성 시간',
    version     int default 0                not null comment '낙관적 락 사용하기 위한 버전 컬럼',
    is_delete   tinyint(1) default 0                 not null comment '삭제 여부 (Y, N)'
);

create table QUEUE
(
    id         bigint auto_increment comment '대기 번호 (PK)'
        primary key,
    user_id    bigint       not null comment '유저 ID (FK)',
    token      varchar(255) not null comment '대기열 토큰',
    status     enum ('WAITING', 'PROGRESS', 'DONE', 'EXPIRED') not null comment '대기열 상태',
    entered_dt datetime     not null comment '대기열 진입 시간',
    expired_dt datetime null comment '대기열 만료 시간',
    constraint fk_queue_user
        foreign key (user_id) references USERS (id)
            on update cascade on delete cascade
);

create table RESERVATION
(
    id                  bigint auto_increment comment '예약 ID (PK)'
        primary key,
    user_id             bigint       not null comment '유저 ID (FK)',
    concert_schedule_id bigint       not null comment '콘서트 일정 ID (FK)',
    seat_id             bigint       not null comment '좌석 ID (FK)',
    concert_title       varchar(255) not null comment '콘서트 제목',
    concert_open_dt     date         not null comment '콘서트 개최 날짜',
    concert_start_dt    datetime     not null comment '콘서트 시작 시간',
    concert_end_dt      datetime     not null comment '콘서트 종료 시간',
    seat_amount         bigint       not null comment '좌석 금액',
    seat_position       int          not null comment '좌석 번호',
    status              enum ('TEMP_RESERVED', 'RESERVED', 'CANCELED') not null comment '예약 상태',
    reserved_dt         datetime     not null comment '예약 시간',
    reserved_until_dt   datetime null comment '예약 만료 시간',
    created_dt          datetime     not null comment '생성 시간',
    is_delete           tinyint(1) default 0                           not null comment '삭제 여부 (Y, N)',
    constraint fk_reservation_schedule
        foreign key (concert_schedule_id) references CONCERT_SCHEDULE (id)
            on update cascade on delete cascade,
    constraint fk_reservation_seat
        foreign key (seat_id) references CONCERT_SEAT (id)
            on update cascade on delete cascade,
    constraint fk_reservation_user
        foreign key (user_id) references USERS (id)
            on update cascade on delete cascade
);

create table PAYMENT
(
    id             bigint auto_increment comment '결제 번호 (PK)'
        primary key,
    user_id        bigint   not null comment '유저 ID (FK)',
    reservation_id bigint   not null comment '예약 ID (FK)',
    price          bigint   not null comment '결제 금액',
    status         enum ('PROGRESS', 'DONE', 'CANCELED') not null comment '결제 상태',
    created_dt     datetime not null comment '결제 시간',
    is_delete      tinyint(1) default 0                  not null comment '삭제 여부 (Y, N)',
    constraint fk_payment_reservation
        foreign key (reservation_id) references RESERVATION (id)
            on update cascade on delete cascade,
    constraint fk_payment_user
        foreign key (user_id) references USERS (id)
            on update cascade on delete cascade
);

create table PAYMENT_HISTORY
(
    id            bigint auto_increment comment '금액 사용 내역 ID (PK)'
        primary key,
    user_id       bigint   not null comment '유저 ID (FK)',
    payment_id    bigint   not null comment '결제 ID (FK)',
    amount_change int      not null comment '금액 변경',
    type          enum ('PAYMENT', 'REFUND') not null comment '금액 사용 타입',
    created_dt    datetime not null comment '금액 변경 시간',
    is_delete     tinyint(1) default 0       not null comment '삭제 여부 (Y, N)',
    constraint fk_payment_history_payment
        foreign key (payment_id) references PAYMENT (id)
            on update cascade on delete cascade,
    constraint fk_payment_history_user
        foreign key (user_id) references USERS (id)
            on update cascade on delete cascade
);

