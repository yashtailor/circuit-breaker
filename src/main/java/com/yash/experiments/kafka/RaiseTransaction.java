package com.yash.experiments.kafka;

import com.yash.experiments.config.CustomCircuitBreaker;
import com.yash.experiments.constants.KafkaConstants;
import com.yash.experiments.events.TxnEvent;
import com.yash.experiments.events.TxnUpdateEvent;
import com.yash.experiments.mongo.Order;
import com.yash.experiments.mongo.OrderRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@Slf4j
public class RaiseTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaiseTransaction.class);

    private final KafkaSender kafkaSender;

    private final RestTemplate restTemplate;

    private final OrderRepository orderRepository;

    private final CircuitBreaker circuitBreaker;

    @Autowired
    public RaiseTransaction(KafkaSender kafkaSender, RestTemplate restTemplate, OrderRepository orderRepository){
        this.kafkaSender = kafkaSender;
        this.restTemplate = restTemplate;
        this.orderRepository = orderRepository;
//        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
//                .failureRateThreshold(50)
//                .enableAutomaticTransitionFromOpenToHalfOpen()
//                .permittedNumberOfCallsInHalfOpenState(2)
//                .slidingWindowSize(2)
//                .waitDurationInOpenState(Duration.ofSeconds(30))
//                .build();
        this.circuitBreaker = CircuitBreakerRegistry.ofDefaults().circuitBreaker(KafkaConstants.RAISE_TXN_TOPIC);
        this.circuitBreaker.getEventPublisher().onStateTransition(transition->{
            log.info("state transition: "+transition.getStateTransition().getFromState()+" -> "+transition.getStateTransition().getToState());
            switch (transition.getStateTransition().getToState()){
                case OPEN:
                    log.info("OPEN");
                case CLOSED:
                case HALF_OPEN:
                        break;
            }
        });
    }

    @KafkaListener(topics = KafkaConstants.RAISE_TXN_TOPIC,
            groupId = KafkaConstants.RAISE_TXN_CONSUMER_GROUP_ID)
//    @CustomCircuitBreaker(name = KafkaConstants.RAISE_TXN_TOPIC)
    public void consume(TxnEvent message) {
                            LOGGER.info(String.format("Message received -> %s", message));
                            LOGGER.info(String.format("Trying to process the txn -> %s", message.getTxnId()));
//                            Order order = Order.builder().amount(message.getAmount()).build();
//                            //        orderRepository.save(order);
//                            //lets call an http endpoint
//                            TxnUpdateEvent response = restTemplate.getForObject("https://randomurlhaha.com", TxnUpdateEvent.class);
//                            TxnUpdateEvent txnUpdateEvent = TxnUpdateEvent.
//                                    builder().
//                                    txnId(message.getTxnId()).
//                                    status("SUCCESS"+response.getStatus()).
//                                    build();
//                            kafkaSender.sendMessage(KafkaConstants.TXN_UPDATE_TOPIC, txnUpdateEvent);
                            throw new RuntimeException();
    }

    public void fallback(TxnEvent event){
        log.info("in the fallback method...");
    }
}
