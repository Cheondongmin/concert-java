package com.hhplus.concert.core.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaConnectionTest {

    private static final Logger log = LoggerFactory.getLogger(KafkaConnectionTest.class);

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private String receivedMessage;


    @Test
    @DisplayName("[성공] 카프카 연동 테스트, 브로커에 test-topic 토픽으로 test-message를 전송하고 최대 10초 대기하면서 listener가 받는 메시지와 일치한지 확인한다.")
    void produceTestMessage() {
        String topic = "test-topic";
        String message = "test-message";

        kafkaTemplate.send(topic, message);
        log.info("토픽 : {} , 보낸 메시지 : {}",topic,message);
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    log.info("받은 메시지 : {}", receivedMessage);
                    assertThat(receivedMessage).isEqualTo(message);
                });
    }

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consumeTestMessage(String message) {
        this.receivedMessage = message;
    }

}

