package com.hhplus.concert.core.infrastructure.payment;

import com.hhplus.concert.core.domain.message.MessageSender;
import com.hhplus.concert.core.domain.payment.PaymentMessageSendEvent;
import com.hhplus.concert.core.infrastructure.kafka.PaymentDlqRetryScheduler;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentKafkaTest {
    @Container
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:latest")
    );

    @Container
    static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:latest")
            .waitingFor(Wait.forListeningPort())
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379));
    }

    @Autowired
    private KafkaTemplate<String, PaymentMessageSendEvent> kafkaTemplate;

    @MockBean
    private MessageSender messageSender;

    @Autowired
    private PaymentDlqRetryScheduler scheduler;

    private PaymentMessageSendEvent testEvent;
    private PaymentMessageSendEvent receivedEvent;
    private PaymentMessageSendEvent dlqReceivedEvent;

    @BeforeEach
    void setUp() {
        testEvent = new PaymentMessageSendEvent(
                "test@example.com",
                "Test Concert",
                LocalDateTime.now().plusDays(7),
                LocalDateTime.now(),
                50000L
        );
    }

    @Nested
    @DisplayName("기본 Kafka 메시지 전송 테스트")
    class BasicKafkaTests {
        @KafkaListener(topics = "payment-notification", groupId = "test-group")
        void consumeTestMessage(PaymentMessageSendEvent event) {
            receivedEvent = event;
        }

        @Test
        @DisplayName("결제 메시지가 정상적으로 전송되고 수신된다")
        void shouldSendAndReceiveMessage() {
            // when
            kafkaTemplate.send("payment-notification", testEvent);

            // then
            await().atMost(ofSeconds(10))
                    .untilAsserted(() -> {
                        assertThat(receivedEvent).isNotNull();
                        assertThat(receivedEvent.mail()).isEqualTo(testEvent.mail());
                        assertThat(receivedEvent.concertTitle()).isEqualTo(testEvent.concertTitle());
                    });
        }
    }

    @Nested
    @DisplayName("DLQ 동작 테스트")
    class DlqOperationTests {
        @KafkaListener(topics = "payment-notification.DLQ", groupId = "dlq-test-group")
        void consumeDlqMessage(PaymentMessageSendEvent event) {
            dlqReceivedEvent = event;
        }

        @Test
        @DisplayName("메시지 처리 실패 시 DLQ로 이동한다")
        void shouldMoveToDeadLetterQueue() throws Exception {
            // given
            doThrow(new RuntimeException("Simulated failure"))
                    .when(messageSender)
                    .sendMessage(anyString());

            // when
            kafkaTemplate.send("payment-notification", testEvent);

            // then
            await().atMost(ofSeconds(10))
                    .untilAsserted(() -> {
                        assertThat(dlqReceivedEvent).isNotNull();
                        assertThat(dlqReceivedEvent.mail()).isEqualTo(testEvent.mail());
                    });
        }
    }

    @Nested
    @DisplayName("DLQ 재처리 테스트")
    class DlqRetryTests {
        @Test
        @DisplayName("최대 재시도 횟수까지 메시지 재처리를 시도한다")
        void shouldRetryUntilMaxAttempts() throws Exception {
            // given
            AtomicInteger retryCount = new AtomicInteger(0);
            doAnswer(invocation -> {
                String message = invocation.getArgument(0);
                // DLQ 재처리 메시지만 카운트 (재시도 메시지에만 포함된 텍스트로 구분)
                if (message.contains("재시도 발송")) {
                    retryCount.incrementAndGet();
                }
                throw new RuntimeException("Persistent failure");
            }).when(messageSender).sendMessage(anyString());

            // when - 실패하는 메시지 발송
            kafkaTemplate.send("payment-notification", testEvent).get();
            Thread.sleep(5000); // DLQ로 이동 대기

            // then - DLQ에서 재시도
            // 5번 시도해도 최대 3번까지
            for (int i = 0; i < 5; i++) {
                scheduler.processFailedMessages();
                Thread.sleep(1000);
            }

            // DLQ 재처리 횟수만 확인
            assertThat(retryCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("재처리 성공 시 정상적으로 처리된다")
        void shouldProcessSuccessfullyOnRetry() throws Exception {
            // given
            AtomicInteger attempts = new AtomicInteger();
            doAnswer(inv -> {
                if (attempts.getAndIncrement() == 0) {
                    throw new RuntimeException("First attempt fails");
                }
                return null;
            }).when(messageSender).sendMessage(anyString());

            // when
            kafkaTemplate.send("payment-notification", testEvent).get();
            Thread.sleep(2000); // DLQ로 이동 대기

            scheduler.processFailedMessages();
            Thread.sleep(1000); // 처리 대기

            // then
            verify(messageSender, times(2)).sendMessage(anyString());
            assertThat(attempts.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("여러 메시지를 순차적으로 처리한다")
        void shouldProcessMultipleMessagesSequentially() throws Exception {
            // given
            doThrow(new RuntimeException("Simulated failure"))
                    .when(messageSender)
                    .sendMessage(anyString());

            // when
            for (int i = 0; i < 3; i++) {
                PaymentMessageSendEvent event = new PaymentMessageSendEvent(
                        "test" + i + "@example.com",
                        "Concert " + i,
                        LocalDateTime.now().plusDays(7),
                        LocalDateTime.now(),
                        50000L
                );
                kafkaTemplate.send("payment-notification", event).get();
            }
            Thread.sleep(2000); // DLQ로 이동 대기

            reset(messageSender); // 실패 설정 초기화
            scheduler.processFailedMessages();
            Thread.sleep(1000);

            // then
            verify(messageSender, times(3)).sendMessage(anyString());
        }
    }

    @AfterAll
    static void stopContainers() {
        KAFKA_CONTAINER.stop();
        REDIS_CONTAINER.stop();
    }
}
