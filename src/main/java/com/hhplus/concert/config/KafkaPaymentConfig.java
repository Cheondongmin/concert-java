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
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> template,
            @Value("${spring.kafka.producer.topic.payment-fail}") String dlqTopic
    ) {
        return new DeadLetterPublishingRecoverer(template,
                (record, exception) -> new TopicPartition(dlqTopic, record.partition()));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentMessageSendEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentMessageSendEvent> consumerFactory,
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentMessageSendEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                deadLetterPublishingRecoverer,
                new FixedBackOff(1000L, 3)
        ));

        return factory;
    }
}