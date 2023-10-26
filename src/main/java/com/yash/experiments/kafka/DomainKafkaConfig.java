package com.yash.experiments.kafka;

import com.yash.experiments.converter.KafkaMessageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class DomainKafkaConfig {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConsumerFactory<Object, Object> consumerFactory,
            KafkaErrorHandler errorHandler,
            KafkaMessageConverter messageConverter) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Set the common error handler for the container factory
        factory.setErrorHandler(errorHandler);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
