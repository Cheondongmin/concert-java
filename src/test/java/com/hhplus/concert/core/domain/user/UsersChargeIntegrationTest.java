package com.hhplus.concert.core.domain.user;

import com.hhplus.concert.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class UsersChargeIntegrationTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void 잔액_충전_동시성_테스트() throws InterruptedException {
        // given
        Users user = new Users(1L, 10000L);
        userRepository.save(user);

        String useToken = "eyJhbGciOiJub25lIn0.eyJ1c2VySWQiOjEsInRva2VuIjoiMzc2NzcxMTctNzZjMy00NjdjLWFmMjEtOTY0ODI3Nzc3YTU3IiwiZW50ZXJlZER0IjoxNzI5MDY3NjIxMTIwLCJleHBpcmVkRHQiOjE3MjkwNjk0MjExMjB9.";

        // when
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> {
                    userService.chargeUserAmount(useToken,1000L);
                }),
                CompletableFuture.runAsync(() -> {
                    userService.chargeUserAmount(useToken,2000L);
                }),
                CompletableFuture.runAsync(() -> {
                    userService.chargeUserAmount(useToken,3000L);
                })
        ).join();

        Thread.sleep(100L);

        // then
        long amount = userService.selectUserAmount(useToken);
        assertThat(amount).isEqualTo(10000 + 1000 + 2000 + 3000);
    }
}