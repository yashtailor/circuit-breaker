package com.yash.experiments.kafka;

import com.yash.experiments.config.CustomCircuitBreakerRegistry;
import com.yash.experiments.constants.KafkaConstants;
import com.yash.experiments.events.CircuitBreakerCloseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CircuitBreakerCloseHandler {
    @Autowired
    CustomCircuitBreakerRegistry circuitBreakerRegistry;
    @KafkaListener(topics = KafkaConstants.CKT_BREAKER_CLOSE_TOPIC,
            groupId = KafkaConstants.CKT_BREAKER_CLOSE_GROUP_ID)
    public void closeCircuitBreaker(CircuitBreakerCloseEvent event){
        log.info("request received for closing the circuit for the topic {}",event.getTopic());
        circuitBreakerRegistry.circuitBreakerRegistry().circuitBreaker(event.getTopic()).transitionToClosedState();
    }
}
