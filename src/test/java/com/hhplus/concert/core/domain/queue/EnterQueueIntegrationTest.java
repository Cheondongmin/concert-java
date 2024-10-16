package com.hhplus.concert.core.domain.queue;

import com.hhplus.concert.IntegrationTest;
import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class EnterQueueIntegrationTest extends IntegrationTest {

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private QueueService queueService;

    @Test
    void 대기열에_존재하지_않을경우_유저를_등록하고_대기열_토큰을_반환한다() {
        // given
        Long userId = 1L;

        // when
        String queueToken = queueService.enterQueue(userId);

        // then
        List<Queue> userQueues = queueRepository.findAll();
        assertAll(
                () -> assertThat(queueToken).isEqualTo(userQueues.get(0).getToken()),
                () -> AssertionsForInterfaceTypes.assertThat(userQueues).hasSize(1)
        );
    }

    @Test
    void 대기열에_존재할_경우_기존_대기열_토큰을_반환한다() {
        // given
        String existQueueToken = "existQueueToken";
        Long userId = 1L;

        queueRepository.save(new Queue(userId, existQueueToken));

        // when
        String queueToken = queueService.enterQueue(userId);

        // then
        List<Queue> userQueues = queueRepository.findAll();
        assertAll(
                () -> assertThat(existQueueToken).isEqualTo(queueToken),
                () -> AssertionsForInterfaceTypes.assertThat(userQueues).hasSize(1)
        );
    }
}
