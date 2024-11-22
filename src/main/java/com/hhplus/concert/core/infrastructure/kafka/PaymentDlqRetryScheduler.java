package com.hhplus.concert.core.infrastructure.kafka;

import com.hhplus.concert.core.domain.message.MessageSender;
import com.hhplus.concert.core.domain.payment.PaymentMessageSendEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentDlqRetryScheduler {
    private final MessageSender messageSender;
    private final KafkaTemplate<String, PaymentMessageSendEvent> kafkaTemplate;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String RETRY_COUNT_HEADER = "retry-count";
    private static final int MAX_RETRY_COUNT = 3;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.producer.topic.payment-fail}")
    private String dlqTopic;

    @Value("${spring.kafka.producer.topic.payment-fail-permanent}")
    private String permanentFailTopic;

    private KafkaConsumer<String, PaymentMessageSendEvent> consumer;

    @PostConstruct
    public void init() {
        this.consumer = new KafkaConsumer<>(getProperties());
        this.consumer.subscribe(Collections.singletonList(dlqTopic));
    }

    @PreDestroy
    public void cleanup() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Scheduled(fixedDelay = 3000) // 5분마다 실행
    public void processFailedMessages() {
        log.info("DLQ 메시지 재처리 시작");
        try {
            ConsumerRecords<String, PaymentMessageSendEvent> records =
                    consumer.poll(Duration.ofSeconds(10));

            if (!records.isEmpty()) {
                for (ConsumerRecord<String, PaymentMessageSendEvent> record : records) {
                    processRecord(record);
                }
                consumer.commitSync();
            }
        } catch (Exception e) {
            log.error("DLQ 메시지 처리 중 예외 발생", e);
        }
    }

    private void processRecord(ConsumerRecord<String, PaymentMessageSendEvent> record) {
        PaymentMessageSendEvent event = record.value();
        int retryCount = getRetryCount(record);

        if (retryCount >= MAX_RETRY_COUNT) {
            handleMaxRetryExceeded(event);
            return;
        }

        try {
            String message = createRetryMessage(event, retryCount);
            messageSender.sendMessage(message);
            log.info("DLQ 메시지 재처리 성공 - Mail: {}, RetryCount: {}",
                    event.mail(), retryCount);

        } catch (Exception e) {
            log.error("DLQ 메시지 재처리 실패 - Mail: {}, RetryCount: {}",
                    event.mail(), retryCount, e);
            sendBackToDlq(event, record.headers(), retryCount);
        }
    }

    private String createRetryMessage(PaymentMessageSendEvent event, int retryCount) {
        return String.format("""
            🎫 콘서트 결제가 완료되었습니다!
            예약자 ID: %s
            콘서트: %s
            시작 날짜: %s
            결제 날짜: %s
            결제 금액: %d원
            콘서트 시작 10분전에는 꼭 입장 부탁드립니다!!
            [재시도 발송 - 시도 횟수: %d]
            """,
                event.mail(),
                event.concertTitle(),
                event.startDt().format(dateFormatter),
                event.confirmDt().format(dateFormatter),
                event.amount(),
                retryCount + 1
        );
    }

    private void sendBackToDlq(PaymentMessageSendEvent event, Headers existingHeaders, int retryCount) {
        ProducerRecord<String, PaymentMessageSendEvent> producerRecord =
                new ProducerRecord<>(dlqTopic, event);
        addRetryCount(producerRecord.headers(), existingHeaders, retryCount);
        kafkaTemplate.send(producerRecord);
    }

    private void handleMaxRetryExceeded(PaymentMessageSendEvent event) {
        log.warn("최대 재시도 횟수 초과 - Mail: {}", event.mail());
        kafkaTemplate.send(permanentFailTopic, event);
    }

    private void addRetryCount(Headers newHeaders, Headers existingHeaders, int currentRetryCount) {
        existingHeaders.forEach(header -> {
            if (!header.key().equals(RETRY_COUNT_HEADER)) {
                newHeaders.add(header);
            }
        });
        newHeaders.add(new RecordHeader(RETRY_COUNT_HEADER,
                String.valueOf(currentRetryCount + 1).getBytes(StandardCharsets.UTF_8)));
    }

    private int getRetryCount(ConsumerRecord<String, PaymentMessageSendEvent> record) {
        Iterator<Header> headers = record.headers().headers(RETRY_COUNT_HEADER).iterator();
        if (headers.hasNext()) {
            Header header = headers.next();
            return Integer.parseInt(new String(header.value(), StandardCharsets.UTF_8));
        }
        return 0;
    }

    private Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-dlq-retry");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }
}