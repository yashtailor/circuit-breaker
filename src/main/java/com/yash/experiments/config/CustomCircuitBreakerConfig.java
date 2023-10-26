package com.yash.experiments.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Configuration
@Slf4j
public class CustomCircuitBreakerConfig {
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CustomCircuitBreakerConfig(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
                                      CircuitBreakerRegistry circuitBreakerRegistry){
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.kafkaListenerEndpointRegistry.getAllListenerContainers().forEach(messageListenerContainer -> {
            log.info("KLC:"+ Arrays.toString(messageListenerContainer.getContainerProperties().getTopics()));
        });
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public void stopKafkaConsumers(CircuitBreaker circuitBreaker){
        kafkaListenerEndpointRegistry.getAllListenerContainers().forEach(messageListenerContainer -> {
           String[] topics = messageListenerContainer.getContainerProperties().getTopics();
            assert topics != null;
            boolean isMatched = Arrays.stream(topics).anyMatch(topic -> circuitBreaker.getName().equals(topic));
            if(isMatched){
                log.info("message listener container paused");
                messageListenerContainer.pause();
            }
        });
    }

    public void resumeKafkaConsumers(CircuitBreaker circuitBreaker, CircuitBreaker.State state){
        kafkaListenerEndpointRegistry.getAllListenerContainers().forEach(messageListenerContainer -> {
            String[] topics = messageListenerContainer.getContainerProperties().getTopics();
            assert topics != null;
            boolean isMatched = Arrays.stream(topics).anyMatch(topic -> circuitBreaker.getName().equals(topic));
            if(isMatched){
                log.info("message listener container resumed");
                messageListenerContainer.resume();
            }
        });
    }

    @PostConstruct
    public void registerOnStateTransition(){
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
          log.info("circuit breaker: "+circuitBreaker.getName());
          circuitBreaker.getEventPublisher().onStateTransition(stateTransition->{
             log.info("STATE TRANSITION FROM: "+stateTransition.getStateTransition().getFromState()+" -> "+stateTransition.getStateTransition().getToState());
             switch (stateTransition.getStateTransition().getToState()){
                 case OPEN:
                     stopKafkaConsumers(circuitBreaker);
                     break;
                 case CLOSED:
                 case HALF_OPEN:
                     resumeKafkaConsumers(circuitBreaker,stateTransition.getStateTransition().getToState());
                     break;
             }
          });
        });
    }

}
