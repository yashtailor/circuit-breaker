package com.yash.experiments.kafka;

import com.yash.experiments.config.CustomCircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class KafkaErrorHandler implements ErrorHandler {

    @Autowired CustomCircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {
        assert data != null;
        log.info("Circuit breaker error handler...."+data.topic());
        circuitBreakerRegistry.circuitBreakerRegistry().circuitBreaker(data.topic()).onError(1, TimeUnit.MINUTES,thrownException);
    }
}
