package com.hhplus.concert.core.domain.user;

import com.hhplus.concert.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class UsersServiceTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final String TEST_TOKEN = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";

    @Nested
    class ConcurrencyTests {
        @Test
        void 잔액_1만원인_유저가_1000원_2000원_3000원을_동시에_충전하면_1만6천원이_된다() throws InterruptedException {
            // given
            Users user = new Users(1L, 10000L);
            userRepository.save(user);

            // when: 1000원, 2000원, 3000원 동시 충전
            CompletableFuture.allOf(
                    CompletableFuture.runAsync(() -> userService.chargeUserAmountOptimisticLock(TEST_TOKEN, 1000L)),
                    CompletableFuture.runAsync(() -> userService.chargeUserAmountOptimisticLock(TEST_TOKEN, 2000L)),
                    CompletableFuture.runAsync(() -> userService.chargeUserAmountOptimisticLock(TEST_TOKEN, 3000L))
            ).join();

            Thread.sleep(100L);

            // then: 10000 + 1000 + 2000 + 3000 = 16000
            long amount = userService.selectUserAmount(TEST_TOKEN);
            assertThat(amount).isEqualTo(16000L);
        }
    }

    @Nested
    class UserAmountTests {
        @Test
        void 잔액이_1000원인_유저가_1000원과_2000원을_충전하면_4000원이_된다() {
            // given: 초기 잔액 1000원
            userRepository.save(new Users(1L, 1000L));

            // when: 1000원, 2000원 순차 충전
            userService.chargeUserAmount(TEST_TOKEN, 1000L);
            userService.chargeUserAmount(TEST_TOKEN, 2000L);
            long userAmount = userService.selectUserAmount(TEST_TOKEN);

            // then: 1000 + 1000 + 2000 = 4000
            assertThat(userAmount).isEqualTo(4000L);
        }
    }
}