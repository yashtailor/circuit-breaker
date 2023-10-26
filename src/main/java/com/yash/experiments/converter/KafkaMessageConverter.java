package com.yash.experiments.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yash.experiments.events.CircuitBreakerCloseEvent;
import com.yash.experiments.events.TxnEvent;
import com.yash.experiments.events.TxnUpdateEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.converter.MessagingMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class KafkaMessageConverter extends MessagingMessageConverter {
    ObjectMapper objectMapper;
    @Autowired
    public KafkaMessageConverter(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }
    private String getMessageTypeFromHeaders(Headers headers){
        AtomicReference<String> messageType = new AtomicReference<>(null);
        headers.headers("message-type").forEach(header -> {
            messageType.set(new String(header.value()));
        });
        return messageType.get();
    }
    @Override
    public Object extractAndConvertValue(ConsumerRecord<?, ?> record, Type type){
        Object result = null;
        String messageType = getMessageTypeFromHeaders(record.headers());
        logger.info("message type:"+messageType);
        logger.info("record:"+record.partition()+":"+record.offset());
        String receivedObject = (String) record.value();
        try{
            switch(messageType){
                case "TxnEvent":
                    result = objectMapper.readValue(receivedObject, TxnEvent.class);
                    break;
                case "TxnUpdateEvent":
                    result = objectMapper.readValue(receivedObject, TxnUpdateEvent.class);
                    break;
                case "CircuitBreakerCloseEvent":
                    result = objectMapper.readValue(receivedObject, CircuitBreakerCloseEvent.class);
                    break;
            }
        }catch (JsonProcessingException e){
            throw new RuntimeException("Json Message received not unable to be processed.");
        }
        if(result == null){
            throw new RuntimeException("I was not able to parse the message to any type");
        }
        return result;
    }
}
