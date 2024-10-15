## 시퀀스 다이어 그램

### 유저 대기열 토큰 기능
```mermaid
sequenceDiagram
    autonumber
    actor user as 사용자
    participant server as API 서버
    participant waitingSystem as 대기열 시스템
    participant QUEUE_SCHEDULER as 대기열 토큰만료 스케쥴러
    participant QUEUE as QUEUE 테이블
    user ->> server: 대기열 토큰 발급 요청
    server ->> waitingSystem: 대기열 토큰 발급 요청
    alt 유저가 대기열에 이미 존재
        waitingSystem -->> server: 기존 대기열 토큰 반환
    else 대기열에 새롭게 들어온 경우
        waitingSystem -->> server: 신규 대기열 토큰 생성 및 반환
    end
    server -->> user: 대기열 토큰 반환
~~~~~~~~~~~~~~~~
    rect rgba(0, 0, 255, .1)
        Note over QUEUE_SCHEDULER: 10초에 한번 호출
        QUEUE_SCHEDULER ->> QUEUE: 만료된 대기열 토큰 확인 (현재시간으로 부터 5분 이상 경과한 PROGRESS 상태)
        QUEUE ->> QUEUE: 대기열 상태 `EXPIRED`로 업데이트
    end
```

### 유저 대기열 토큰 체크 (풀링용)
```mermaid
sequenceDiagram
    autonumber
    actor user as 사용자
    participant API as API 서버
    participant AUTH as 토큰 인증 시스템
    participant QUEUE as QUEUE 테이블
    participant SEAT as CONCERT_SEAT 테이블

    loop 5초마다 확인
        Note over user, API: Authorization 헤더에 token 포함
        API ->> AUTH: token 상태 확인
        AUTH -->> API: token 상태 응답
        alt token 만료
            API -->> user: 에러 - token 만료 (status: EXPIRED)
        else 정상 토큰
            API ->> QUEUE: 현재 PROGRESS 상태 유저 수 확인
            alt 활성화된 유저 수 N명 미만
                API ->> QUEUE: 유저 대기열 상태 확인 (유저 진입 시간과 대기 순번 정보 조회)
                alt 유저가 대기열 통과 가능 (제한 인원 미달 또는 자리가 생김)
                    QUEUE -->> QUEUE: 토큰 만료 시간 10분 연장, 상태 PROGRESS로 업데이트
                    QUEUE -->> API: 유저의 현재 대기 순번 및 status 반환
                    API -->> user: 예약 가능 응답 (status: PROGRESS)
                else 대기 중 (유저 순번이 아직 도달하지 않음)
                    QUEUE -->> QUEUE: 대기 순번 재계산 (유저보다 먼저 들어온 사람 수 확인 후 + 1)
                    QUEUE -->> API: 유저 대기 순번 및 status 반환
                    API -->> user: 대기 중 응답 (status: WAITING)
                end
            else 활성화된 유저 수 N명 초과
                API -->> user: 대기 중 응답 (status: WAITING)
            end
        end
    end

    alt 대기열에서 자리가 생김 (예: 좌석 취소 또는 임시 예약 만료)
        Note over API, QUEUE: 자리가 생긴 상황 감지
        SEAT ->> QUEUE: 좌석 상태 변경 (TEMP_RESERVED -> AVAILABLE)
        QUEUE -->> QUEUE: 가장 오래된 유저 상태를 PROGRESS로 변경
        API -->> user: 대기열 통과 가능 응답 (status: PROGRESS)
    end

```

### 예약 가능한 콘서트의 일정 조회 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant AUTH as 토큰 인증 시스템
    participant CONCERT_SCHEDULE as CONCERT_SCHEDULE 테이블
    사용자 ->> API: 예약 가능한 콘서트의 일정 조회 요청
    Note over 사용자, API: Authorization 헤더에 token 포함
    API ->> AUTH: token 상태 확인
    AUTH -->> API: token 상태 응답
    alt token 만료
        API -->> 사용자: 에러 - token 만료 에러 발생
    else 정상토큰
        alt 대기열 통과 안된 유저
        API -->> 사용자: 에러 - 대기열 틍록 안내 응답
        else 대기열 통과가 된 유저
            API ->> CONCERT_SCHEDULE: 예약 가능한 콘서트의 일정 조회 요청
            alt 예약 가능한 날짜의 콘서트 없음
                CONCERT_SCHEDULE -->> API: 예약 가능한 콘서트의 일정 없음 반환
                API -->> 사용자: 에러 - 예약 가능한 콘서트의 일정 없음
            else 예약 가능한 날짜의 콘서트 존재
                CONCERT_SCHEDULE -->> API: 예약 가능한 콘서트의 일정 및 매진여부 반환
                API -->> 사용자: 예약 가능한 콘서트의 일정 및 매진여부 응답
            end
        end
    end
```

### 좌석 조회 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant CONCERT_SEAT as CONCERT_SEAT 테이블
    participant AUTH as 토큰 인증 시스템
    사용자 ->> API: 특정 콘서트의 예약 가능 좌석 조회 요청
    Note over 사용자, API: Authorization 헤더에 token 포함
    API ->> AUTH: token 상태 확인
    AUTH -->> API: token 상태 응답
    alt token 만료
        API -->> 사용자: 에러 - token 만료 에러 발생 
    else 대기열 통과 안된 유저 (status: WAITING or DONE or EXPIRED)
        API -->> 사용자: 에러 - 대기열 등록 안내 응답 
    else 대기열 통과가 된 유저
        API ->> CONCERT_SEAT: 예약 가능한 좌석 조회 요청
        alt 예약 가능한 좌석 없음
            CONCERT_SEAT -->> API: 예약 가능한 좌석 없음 반환
            API -->> 사용자: 에러 - 예약 가능한 좌석 없음
        else 예약 가능한 좌석이 존재
            CONCERT_SEAT -->> API: 예약 가능한 좌석과 seat_status 반환
            API -->> 사용자: 예약 가능한 좌석과 seat_status 응답
        end
    end
```

### 좌석 예약 요청 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant AUTH as 토큰 인증 시스템
    participant QUEUE as QUEUE 테이블
    participant CONCERT_SEAT as CONCERT_SEAT 테이블
    participant RESERVATION as RESERVATION 테이블
    participant SEAT_SCHEDULER as 좌석 임시 배정 스케줄러
    
    사용자 ->> API: 날짜와 좌석 정보 입력하여 좌석 예약 API 요청
    Note over 사용자, API: Authorization에 token 포함
    API ->> AUTH: token 상태 확인
    AUTH -->> API: token 상태 응답
    alt token 만료
        API -->> 사용자: 에러 응답 (status: EXPIRED)
    else
        API ->> QUEUE: 유저 대기열 상태 확인 (concert_schedule_id, user_id)
        QUEUE -->> API: 대기열 상태 응답 (WAITING, PROGRESS, EXPIRED)

        alt 대기열 status가 WAITING인 경우
            API -->> 사용자: 에러 응답 (status: WAITING)
        else 대기열 status가 EXPIRED인 경우
            API -->> 사용자: 에러 응답 (status: EXPIRED)
        else 대기열 status가 PROGRESS인 경우
            critical 좌석 임시 예약
                API ->> CONCERT_SEAT: 좌석 예약 요청 (concert_schedule_id, seat_id 포함)
                alt 좌석이 이미 예약된 경우
                    CONCERT_SEAT -->> API: 에러 응답 (좌석이 이미 예약됨)
                    API -->> 사용자: 에러 응답 (좌석이 이미 예약됨)
                else 좌석 예약 가능한 경우
                    API ->> RESERVATION: 예약 데이터 삽입 (status: TEMP_RESERVED)
                    API -->> CONCERT_SEAT: 좌석 데이터 임시예약 전환 (seat_status: TEMP_RESERVED)
                    API -->> 사용자: 좌석 임시 예약 성공 응답
                end
            end
        end
    end

    rect rgba(0, 0, 255, .1)
        SEAT_SCHEDULER ->> CONCERT_SEAT: 임시 예약된 좌석 중 5분 내 결제가 완료되지 않은 좌석의 상태 해제 (seat_status: AVAILABLE)
        CONCERT_SEAT ->> CONCERT_SEAT: 좌석 데이터 seat_status 변경 (임시 예약 해제)
    end
```

### 잔액 충전 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant AUTH as 토큰 인증 시스템
    participant USERS as USERS 테이블
    participant PAYMENT_HISTORY as PAYMENT_HISTORY 테이블

    사용자 ->> API: 잔액 충전 API 요청
    Note over 사용자, API: Authorization에 token 포함
    API ->> AUTH: 토큰 인증 요청
    AUTH -->> API: 토큰 인증 성공

    API ->> USERS: 잔액 충전 요청 (user_id, 충전 금액 포함)
    USERS ->> USERS: 유저 존재 여부 확인 (user_id 확인)

    alt 유저가 존재하지 않을 경우
        USERS -->> API: 에러 응답 (유저 없음)
        API -->> 사용자: 에러 응답 (유저 없음)
    else 유저가 존재할 경우
        USERS ->> USERS: 충전 금액이 0 이상인지 확인
        alt 충전 금액이 0 이하일 경우
            USERS -->> API: 에러 응답 (충전 금액이 0 이하)
            API -->> 사용자: 에러 응답 (충전 금액이 0 이하)
        else 충전 금액이 0 이상일 경우
            USERS -->> API: 충전 성공 응답 (updated 잔액)
            API ->> PAYMENT_HISTORY: 잔액 사용 내역 기록 요청 (user_id, amount_change, type: PAYMENT)
            PAYMENT_HISTORY -->> API: 기록 성공 응답
            API -->> 사용자: 충전 성공 응답 (updated 잔액)
        end
    end

```

### 잔액 조회 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant AUTH as 토큰 인증 시스템
    participant USERS as USERS 테이블

    사용자 ->> API: 잔액 조회 API 요청
    Note over 사용자, API: Authorization에 token 포함
    API ->> AUTH: 토큰 인증 요청
    AUTH -->> API: 토큰 인증 성공

    API ->> USERS: 잔액 조회 요청 (user_id 포함)
    USERS ->> USERS: 유저 존재 여부 확인 (user_id 확인)

    alt 유저가 존재하지 않을 경우
        USERS -->> API: 에러 응답 (유저 없음)
        API -->> 사용자: 에러 응답 (유저 없음)
    else 유저가 존재할 경우
        USERS -->> API: 잔액 반환 (amount)
        API -->> 사용자: 잔액 응답 (amount)
    end
```

### 결제 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant AUTH as 토큰 인증 시스템
    participant USERS as USERS 테이블
    participant QUEUE as QUEUE 테이블
    participant CONCERT_SEAT as CONCERT_SEAT 테이블
    participant PAYMENT as PAYMENT 테이블
    participant PAYMENT_HISTORY as PAYMENT_HISTORY 테이블
    participant RESERVATION as RESERVATION 테이블

    사용자 ->> API: 결제 API 요청 (reservation_id 포함)
    Note over 사용자, API: Authorization에 token 포함
    API ->> AUTH: token 상태 확인
    AUTH -->> API: token 상태 응답

    API ->> QUEUE: 유저 대기열 상태 확인 (user_id, concert_schedule_id)
    QUEUE -->> API: 대기열 상태 응답 (PROGRESS)

    alt 대기열 상태가 PROGRESS인 경우
        API ->> USERS: 잔액 확인 요청 (user_id 포함)
        USERS ->> USERS: 유저 존재 여부 및 잔액 확인

        alt 유저 잔액이 부족한 경우
            USERS -->> API: 에러 응답 (잔액 부족)
            API -->> 사용자: 에러 응답 (잔액 부족)
        else 유저 잔액이 충분한 경우
            PAYMENT ->> RESERVATION: 예약 상태 확인 (reservation_id 확인)
            alt 예약 상태가 유효하지 않은 경우
                RESERVATION -->> PAYMENT: 에러 응답 (유효하지 않은 예약)
                PAYMENT -->> API: 에러 응답 (유효하지 않은 예약)
                API -->> 사용자: 에러 응답 (유효하지 않은 예약)
            else 예약 상태가 유효한 경우
                API ->> USERS: 잔액 차감 요청 (user_id, 결제 금액 차감)
                USERS -->> API: 잔액 차감 완료
                API ->> PAYMENT_HISTORY: 결제 내역 기록 (user_id, amount_change, type: PAYMENT)
                PAYMENT_HISTORY -->> API: 기록 성공 응답

                API ->> CONCERT_SEAT: 좌석 상태 업데이트 (seat_id, TEMP_RESERVED -> RESERVED)
                CONCERT_SEAT -->> API: 좌석 상태 업데이트 성공 응답

                API ->> PAYMENT: 결제 상태 `DONE`으로 업데이트
                PAYMENT -->> API: 결제 상태 업데이트 완료

                API ->> QUEUE: 대기열 상태를 `DONE`으로 업데이트
                QUEUE -->> API: 대기열 상태 업데이트 완료

                API -->> 사용자: 결제 완료 및 좌석 예약 성공 응답
            end
        end
    else 대기열 상태가 유효하지 않은 경우
        API -->> 사용자: 에러 응답 (대기열 상태 오류)
    end
```
