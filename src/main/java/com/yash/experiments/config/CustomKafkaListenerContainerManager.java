package com.yash.experiments.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.event.ConsumerStartingEvent;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CustomKafkaListenerContainerManager implements ApplicationListener<ConsumerStartingEvent> {

    @Autowired CustomCircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Getter public HashMap<String, Integer> errorManager = new HashMap<>();

    @Getter public static final int THRESHOLD = 5;

    public MessageListenerContainer getMessageListenerContainer(String topic){
        var container = kafkaListenerEndpointRegistry
                .getAllListenerContainers()
                .stream()
                .filter(messageListenerContainer -> {
                    String[] topics = messageListenerContainer.getContainerProperties().getTopics();
                    if(topics == null)return false;
                    return Arrays.asList(topics).contains(topic);
                }).collect(Collectors.toList());
        return container.size() > 0 ? container.get(0) : null;
    }

    public void pauseKafkaConsumers(String name, CircuitBreaker.State state){
        log.info("circuit breaker in the state: {}",state.name());
        MessageListenerContainer container = getMessageListenerContainer(name);
        if(container==null){
            log.info("No container found with the name {}",name);
            return;
        }
        container.pause();
    }

    public void resumeKafkaConsumers(String name, CircuitBreaker.State state){
        log.info("circuit breaker in the state: {}",state.name());
        MessageListenerContainer container = getMessageListenerContainer(name);
        if(container==null){
            log.info("No container found with the name {}",name);
            return;
        }
        container.resume();
    }

    public void addCktBreaker(String name){
        circuitBreakerRegistry.circuitBreakerRegistry().circuitBreaker(name).getEventPublisher().onStateTransition(transition->{
            log.info("state transition: "+transition.getStateTransition().getFromState()+" -> "+transition.getStateTransition().getToState());
            switch (transition.getStateTransition().getToState()){
                case OPEN:
                    pauseKafkaConsumers(name, CircuitBreaker.State.OPEN);
                    break;
                case HALF_OPEN:
                    if(errorManager.containsKey(name)){
                        if(errorManager.get(name) >= THRESHOLD){
                            log.info("circuit breaker tried to move to HALF OPEN but the threshold limit is reached");
                            circuitBreakerRegistry.circuitBreakerRegistry().circuitBreaker(name).transitionToOpenState();
                            break;
                        }
                    }
                    resumeKafkaConsumers(name, CircuitBreaker.State.HALF_OPEN);
                    break;
                case CLOSED:
                    errorManager.put(name, 0);
                    // technically should never be required as the circuit breaker
                    // won't directly transition from open to close state
                    // it will always go via half open state
                    // the only condition it is moving from open to closed state
                    // is during manual trigger
                    if(transition.getStateTransition().getFromState() == CircuitBreaker.State.OPEN)
                        resumeKafkaConsumers(name, CircuitBreaker.State.CLOSED);
                    break;
                case FORCED_OPEN:
                    log.info("Circuit breaker in forced open state now...");
            }
        }).onError(event -> {
            if(errorManager.containsKey(name)){
                int reachedCount = errorManager.get(name)+1;
                errorManager.put(name, reachedCount);
                log.info("registry: {} {} {}",name,reachedCount,THRESHOLD);
                if(reachedCount >= THRESHOLD){
                    circuitBreakerRegistry.circuitBreakerRegistry().circuitBreaker(name).transitionToOpenState();
                }
            }
            else errorManager.put(name, 1);
        });
    }

    // Whenever a new consumer is starting, we configure a circuit breaker for it
    @Override
    public void onApplicationEvent(ConsumerStartingEvent event) {
        List<KafkaMessageListenerContainer> containerList = event.getContainer(ConcurrentMessageListenerContainer.class).getContainers();
        containerList.forEach(container -> {
            Optional<String> topicName = Arrays.stream(Objects.requireNonNull(container.getContainerProperties().getTopics())).findAny();
            topicName.ifPresent(this::addCktBreaker);
        });
    }
}