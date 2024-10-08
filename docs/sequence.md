## 시퀀스 다이어 그램

### 유저 대기열 토큰 기능
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant 대기열 as 대기열 시스템
    사용자 ->> API: 대기열 토큰 생성 요청
    API ->> 대기열: 대기열에 유저 등록 요청
    alt 유저가 이미 대기열에 있을 경우
        대기열 -->> API: 기존 대기열 토큰 반환
    else 유저가 대기열에 없을 경우
        대기열 -->> API: 신규 대기열 토큰 생성 및 반환
    end
    API -->> 사용자: 대기열 토큰 반환

    loop 1초마다 대기 순번 확인
        사용자 ->> API: 대기열 순번 확인 요청 (토큰 포함)
        API ->> 대기열: 대기열 상태 확인 요청
        alt 대기열을 통과할 수 있을 경우
            대기열 -->> API: 유저가 대기열 통과 (예약 가능)
            API -->> 사용자: 예약 가능 상태 응답 (대기열 통과)
        else 대기열을 대기해야 할 경우
            대기열 -->> API: 유저의 현재 대기 순번 반환
            API -->> 사용자: 현재 대기 순번 응답
        end
    end
```


### 예약 가능 날짜 조회 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant 날짜 as 날짜 서비스
    participant 인증 as 토큰 인증 시스템
    사용자 ->> API: 예약 가능한 날짜 조회 요청
    Note over 사용자, API: Authorization 헤더에 토큰 포함
    API ->> 인증: 토큰 상태 확인
    인증 -->> API: 토큰 상태 응답
    alt 토큰 만료
        API -->> 사용자: 에러 - 토큰 만료 에러 발생
    else 대기열 통과 안된 유저
        API -->> 사용자: 에러 - 대기열 등록 안내 응답
    else 대기열 통과가 된 유저
        API ->> 날짜: 예약 가능한 날짜 조회
        alt 예약 가능한 날짜 없음
            API -->> 사용자: 에러 - 예약 가능한 날짜 없음
        else 예약 가능한 날짜가 존재
            날짜 -->> API: 예약 가능한 날짜 응답
            API -->> 사용자: 예약 가능한 날짜 응답
        end
    end
```

### 좌석 조회 API
```mermaid
sequenceDiagram
    autonumber
    actor 사용자 as 사용자
    participant API as API 서버
    participant 좌석 as 좌석 서비스
    participant 인증 as 토큰 인증 시스템
    사용자 ->> API: 특정 날짜의 예약 가능 좌석 조회 요청
    Note over 사용자, API: Authorization 헤더에 토큰 포함
    API ->> 인증: 토큰 상태 확인
    인증 -->> API: 토큰 상태 응답
    alt 토큰 만료
        API -->> 사용자: 에러 - 토큰 만료 에러 발생
    else 대기열 통과 안된 유저
        API -->> 사용자: 에러 - 대기열 등록 안내 응답
    else 대기열 통과가 된 유저
        API ->> 좌석: 예약 가능한 좌석 조회
        alt 예약 가능한 좌석 없음
            API -->> 사용자: 에러 - 예약 가능한 좌석 없음
        else 예약 가능한 좌석이 존재
            좌석 -->> API: 예약 가능한 좌석 응답
            API -->> 사용자: 예약 가능한 좌석 응답
        end
    end
```



