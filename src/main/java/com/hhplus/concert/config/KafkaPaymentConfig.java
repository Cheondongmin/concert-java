package com.hhplus.concert.config;

import com.hhplus.concert.core.domain.payment.PaymentMessageSendEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class KafkaPaymentConfig {

    @Value("${spring.kafka.producer.topic.payment-success}")
    private String paymentSuccessTopic;

    @Value("${spring.kafka.producer.topic.payment-fail}")
    private String paymentFailTopic;

    @Value("${spring.kafka.producer.topic.payment-fail-permanent}")
    private String permanentFailTopic;

    @Bean
    public NewTopic paymentSuccessTopic() {
        // 토픽, 생성할 파티션 개수(로드밸런서처럼 라운드-로빈으로 동작함), 레플리케이션팩터(복제본 생성 개수)
        return new NewTopic(paymentSuccessTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic paymentNotificationDlqTopic() {
        return new NewTopic(paymentFailTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic paymentNotificationDlqPermanentTopic() {
        return new NewTopic(permanentFailTopic, 3, (short) 1);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<Object, Object> template) {
        return new DeadLetterPublishingRecoverer(template, (record, exception) -> new TopicPartition("payment-notification.DLQ", record.partition()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentMessageSendEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentMessageSendEvent> consumerFactory,
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentMessageSendEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);

        // 에러 핸들러 설정
        factory.setCommonErrorHandler(new DefaultErrorHandler(deadLetterPublishingRecoverer, new FixedBackOff(1000L, 3)));

        return factory;
    }
}