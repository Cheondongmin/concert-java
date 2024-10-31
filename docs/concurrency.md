# 동시성 시나리오 분석
## 유저 잔액 충전
### 정상 시나리오
![동시성제어_정상](https://github.com/user-attachments/assets/0d717b52-642a-4bdd-b9ae-878f93bc6850)

### 동시성 문제 발생 케이스
![동시성제어_분실갱신](https://github.com/user-attachments/assets/16b53257-1453-4133-bf29-fb8d0501da16)

### 비스니스 로직 순서
- 조회 후 갱신이기 때문에 위와 같은 동시성 문제가 발생할 수 있다.<br>
  따라서 잔액 충전 요청의 정합성이 깨지는 현상 (Lost Update)갱신 손실이 발생할 수 있다.

### 적용한 락
- 낙관적 락 사용

### 낙관적 락 선택 배경
- 잔액 충전은 사용자 개인별로 이루어지는 작업으로, 동일 계정에 대한 동시 요청 빈도가 낮다.
- 대부분의 경우 충돌이 발생하지 않을 것이라 예상되어 낙관적 락이 더 효율적이다.
- 데이터베이스 연결을 최소화하여 리소스를 효율적으로 사용하고자 한다.
- 트랜잭션 시간을 최소화하여 전반적인 시스템 성능을 개선하고자 한다.
- 데드락 발생 가능성이 없다.

### 잔액충전 동시성 제어 시나리오 검증
- 소규모 동시성 (10개 요청)
- 중규모 동시성 (100개 요청)
- 대규모 동시성 (1000개 요청)

(테스트 로직은 아래와 같이 작성)
```markdown
```java
  @Test
  void 잔액이_1만원인_유저가_100번_동시에_충전요청을_보낸다() throws InterruptedException {
      // given
      Users user = new Users(1L, 10000L);
      userRepository.save(user);
      int numberOfRequests = 100;
      Long chargeAmount = 1000L;
      CountDownLatch latch = new CountDownLatch(numberOfRequests);

      // when
      CompletableFuture<Void>[] futures = new CompletableFuture[numberOfRequests];
      for(int i = 0; i < numberOfRequests; i++) {
          futures[i] = CompletableFuture.runAsync(() -> {
              try {
                  userService.chargeUserAmountOptimisticLock(TEST_TOKEN, chargeAmount);
              } finally {
                  latch.countDown();
              }
          });
      }

      // 모든 요청이 완료될 때까지 대기
      latch.await(5, TimeUnit.SECONDS);

      // then
      Users updatedUser = userRepository.findById(1L);
      assertThat(updatedUser.getUserAmount()).isEqualTo(110000L);
  }
```

| 스레드 수              | 낙관적 락 | 비관적 락 | redis 분삭락 |
|----------------------|--------|-------|-------|
| 스레드 10개시 수행 속도   | 0.5s   | 0.6s  | 0.6s  |
| 스레드 100개시 수행 속도  | 0.8s   | 0.7s  | 1.2s  |
| 스레드 1000개시 수행 속도 | 2.3s   | 2.1s  | 4.6s  |

- 낙관적 락의 경우 스레드 요청수를 증가시킬 수록 적절한 재시도 횟수를 찾아야 한다.
- 스레드 요청이 많을 경우 낙관적 락의 수행 속도가 더 오래 걸리는 결과가 나왔다.

## 좌석 예약
### 정상 시나리오
![좌석예약_동시성](https://github.com/user-attachments/assets/2aa1c1ae-058d-45fc-9714-60174f6d0199)

### 동시성 문제 발생 케이스
![좌석예약_동시성_실패](https://github.com/user-attachments/assets/5b6bce75-0235-4bf3-916d-f11910960f57)

### 비즈니스 로직 순서
- 예약 정보 조회
- 이미 예약이 된 좌석인지 검증
- 예약 성공 혹은 실패

### 적용한 락
- 낙관적 락 사용

### 낙관적 락 선택 배경
- 좌석 예약은 "선착순" 특성
- 동시 요청 중 한 명만 성공하면 됨

### 예약 동시성 제어 시나리오 검증
- 소규모 동시성 (10개 요청)
- 중규모 동시성 (100개 요청)
- 대규모 동시성 (1000개 요청)

(테스트 로직은 아래와 같이 작성)
```markdown
```java
   @Test
    void 동시에_100개의_요청이_들어올때_하나의_좌석은_한번만_예약된다() throws InterruptedException {
        // given
        int numberOfThreads = 100;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        Users user = new Users(1L, 1000L); // 유저 잔액은 1000
        userRepository.save(user);
        String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
        Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
        queueRepository.save(queue);

        // 콘서트, 콘서트 스케줄 및 좌석 설정
        Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
        concertRepository.save(concert);
        ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDateTime.now().toLocalDate(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
        concertScheduleRepository.save(concertSchedule);
        ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
        concertSeatRepository.save(concertSeat);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    // when
                    concertService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        List<Reservation> reservations = reservationRepository.findAll();
        AssertionsForInterfaceTypes.assertThat(reservations).hasSize(1);
    }
```

| 스레드 수              | 낙관적 락 | 비관적 락 | redis 분삭락 |
|----------------------|--------|-------|-------|
| 스레드 10개시 수행 속도   | 0.5s   | 0.5s  | 0.6s  |
| 스레드 100개시 수행 속도  | 0.7s   | 0.7s  | 1.2s  |
| 스레드 1000개시 수행 속도 | 1.3s   | 1.7s  | 6.5s  |

## 좌석 결제
### 정상 시나리오
![image](https://github.com/user-attachments/assets/132ab87b-61bb-4615-96f5-926ecbdd453b)

### 동시성 문제 발생 케이스
![image](https://github.com/user-attachments/assets/7eb2667a-51f1-4299-b53d-5d98fd97c428)

### 비즈니스 로직 순서
- 잔액 체크 후 결제가 가능한 상태인지 검증
- 콘서트가 예약(점유) 상태인제 검증
- 잔액 차감 후 예약 완료처리 진행

### 적용한 락
- 낙관적 락 사용

### 낙관적 락 선택 배경
- 대부분의 결제 시도는 동일한 사용자에 대한 것
- 일반적으로 한 사용자가 동일한 예약에 대해 동시에 여러 번 결제를 시도하는 경우는 드뭄
- 실제 충돌이 적은 상황에서는 낙관적 락이 더 효율적

### 예약 동시성 제어 시나리오 검증
- 소규모 동시성 (10개 요청)
- 중규모 동시성 (100개 요청)
- 대규모 동시성 (1000개 요청)

(테스트 로직은 아래와 같이 작성)
```markdown
```java
  @Test
  void 동일_유저가_100번_결제요청을_해도_한번만_정상적으로_처리된다() throws InterruptedException {
      // given
      Users user = new Users(1L, 1000L); // 유저 잔액은 1000
      userRepository.save(user);
      String token = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";
      Queue queue = new Queue(user.getId(), token, QueueStatus.PROGRESS, null);
      queueRepository.save(queue);

      // 콘서트, 콘서트 스케줄 및 좌석 설정
      Concert concert = new Concert(1L, "testConcert", LocalDateTime.now(), false);
      concertRepository.save(concert);
      ConcertSchedule concertSchedule = new ConcertSchedule(1L, concert.getId(), LocalDate.now(), LocalDateTime.now(), LocalDateTime.now().plusHours(2), 50, 50, TotalSeatStatus.AVAILABLE, LocalDateTime.now(), false);
      concertScheduleRepository.save(concertSchedule);
      ConcertSeat concertSeat = new ConcertSeat(1L, concertSchedule.getId(), 500L, 1, SeatStatus.AVAILABLE, null, LocalDateTime.now(), false);
      concertSeatRepository.save(concertSeat);

      // 예약 생성 및 저장
      ReserveConcertResult result = reservationService.reserveConcert(token, concertSchedule.getId(), concertSeat.getId());

      // 동시성 제어를 위한 ExecutorService 설정
      int threadCount = 100; // 동일한 유저가 100번 결제 요청
      ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
      CountDownLatch latch = new CountDownLatch(threadCount);

      // when
      for (int i = 0; i < threadCount; i++) {
          executorService.submit(() -> {
              try {
                  paymentService.paymentConcert(token, result.reservationId());
              } finally {
                  latch.countDown(); // 스레드가 종료되면 카운트 감소
              }
          });
      }

      latch.await(); // 모든 스레드가 완료될 때까지 대기

      // then
      Users updatedUser = userRepository.findById(user.getId());
      ConcertSeat updatedSeat = concertSeatRepository.findById(concertSeat.getId());
      List<Payment> paymentList = paymentRepository.findAll();

      // 한 번만 결제가 성공적으로 완료되어야 함]
      assertThat(paymentList.size()).isEqualTo(1); // payment는 한번만 등록되어야 함
      assertThat(paymentList.get(0).getPrice()).isEqualTo(500L);
      assertThat(updatedUser.getUserAmount()).isEqualTo(500L); // 잔액이 500으로 감소해야 함
      assertThat(updatedSeat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED); // 좌석 상태가 RESERVED로 변경
  }
```

| 스레드 수              | 낙관적 락 | 비관적 락 | redis 분삭락 |
|----------------------|--------|-------|-------|
| 스레드 10개시 수행 속도   | 0.4s   | 0.5s  | 0.5s  |
| 스레드 100개시 수행 속도  | 0.5s   | 0.6s  | 1.2s  |
| 스레드 1000개시 수행 속도 | 1.9s   | 1.3s  | 4.9s  |

- 스레드 요청이 많을 경우 낙관적 락의 수행 속도가 더 오래 걸리는 결과가 나왔다.

## 종합

### 장점

낙관적 락:
- 적은 리소스 사용 (DB 락 불필요)
- 낮은 트래픽에서 최고 성능
- 구현 단순성 (버전 필드만 추가)

비관적 락:
- 높은 트래픽에서 안정적 성능
- 실패/재시도 로직 불필요
- 트랜잭션 롤백 비용 최소화

Redis 분산락:
- 분산 환경에서 확장성
-----
### 단점

낙관적 락:
- 높은 트래픽시 재시도 로직 필요
- 충돌 증가에 따른 성능 저하
- 리소스 낭비 (실패한 트랜잭션)

비관적 락:
- DB 리소스 점유
- 데드락 가능성

Redis 분산락:
- 추가 인프라 필요
- 높은 레이턴시
- 구현/운영 복잡도 증가
