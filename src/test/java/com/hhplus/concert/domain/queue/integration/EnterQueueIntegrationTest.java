package com.hhplus.concert.domain.queue.integration;

import com.hhplus.concert.IntegrationTest;
import com.hhplus.concert.app.domain.queue.entlty.Queue;
import com.hhplus.concert.app.domain.queue.repository.QueueRepository;
import com.hhplus.concert.app.domain.queue.service.QueueEnterService;
import com.hhplus.concert.app.domain.user.repository.UserRepository;
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
    private UserRepository userRepository;

    @Test
    void 대기열에_존재하지_않을경우_유저를_등록하고_대기열_토큰을_반환한다() {
        // given
        Long userId = 1L;

        // 먼저 User를 저장해야 함
        userRepository.save(userId); // User 엔티티를 저장하는 코드가 필요

        QueueEnterService queueEnterService = new QueueEnterService(queueRepository);

        // when
        String queueToken = queueEnterService.enterQueue(userId);

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
        QueueEnterService queueEnterService = new QueueEnterService(queueRepository);
        Long userId = 1L;

        // 먼저 User를 저장해야 함
        userRepository.save(userId); // User 엔티티를 저장하는 코드가 필요

        queueRepository.save(new Queue(userId, existQueueToken));

        // when
        String queueToken = queueEnterService.enterQueue(userId);

        // then
        List<Queue> userQueues = queueRepository.findAll();
        assertAll(
                () -> assertThat(existQueueToken).isEqualTo(queueToken),
                () -> AssertionsForInterfaceTypes.assertThat(userQueues).hasSize(1)
        );
    }
}
