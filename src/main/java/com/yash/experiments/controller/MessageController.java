package com.yash.experiments.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yash.experiments.config.CustomKafkaListenerContainerManager;
import com.yash.experiments.constants.KafkaConstants;
import com.yash.experiments.events.CircuitBreakerCloseEvent;
import com.yash.experiments.events.TxnEvent;
import com.yash.experiments.kafka.KafkaSender;
import com.yash.experiments.models.CircuitBreakerError;
import com.yash.experiments.models.CircuitBreakerStatus;
import com.yash.experiments.models.TxnRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1/kafka")
public class MessageController {

    @Autowired private KafkaSender kafkaSender;

    @Autowired private CircuitBreakerRegistry registry;

    @Autowired private CustomKafkaListenerContainerManager manager;

    @PostMapping(value = "/raise-txn")
    public ResponseEntity<String> raiseTxn(@RequestBody TxnRequest request) throws JsonProcessingException {
        String txnId = UUID.randomUUID().toString();
        TxnEvent txnEvent = TxnEvent.builder().
                creditorAccountNm(request.getCreditorAccountNm()).
                debtorAccountNm(request.getDebtorAccountNm()).
                amount(request.getAmount()).
                txnId(txnId).
                build();
        kafkaSender.sendMessage(KafkaConstants.RAISE_TXN_TOPIC,txnEvent);
        return ResponseEntity.ok("Txn Raised. Txn Id = "+txnId);
    }

    @PostMapping(value = "/close-ckt-breaker")
    public boolean close(@RequestBody String topic){
        kafkaSender.sendMessage(KafkaConstants.CKT_BREAKER_CLOSE_TOPIC,
                CircuitBreakerCloseEvent.builder().topic(topic).build());
        return true;
    }

    private CircuitBreakerError getError(CircuitBreaker circuitBreaker){
        if(manager.getErrorManager().containsKey(circuitBreaker.getName())){
            return CircuitBreakerError.builder()
                    .thresholdReached(manager.getErrorManager().get(circuitBreaker.getName()) >= CustomKafkaListenerContainerManager.getTHRESHOLD())
                    .errorCount(manager.getErrorManager().get(circuitBreaker.getName()))
                    .build();
        }
        return CircuitBreakerError.builder()
                .thresholdReached(false)
                .errorCount(0)
                .build();
    }

    @GetMapping(value = "/ckt-breaker-status")
    public List<CircuitBreakerStatus> status(){
        return registry.getAllCircuitBreakers().map(circuitBreaker ->
                CircuitBreakerStatus
                        .builder()
                        .name(circuitBreaker.getName())
                        .status(circuitBreaker.getState().name())
                        .error(getError(circuitBreaker))
                        .build()
        ).asJava();
    }
}
