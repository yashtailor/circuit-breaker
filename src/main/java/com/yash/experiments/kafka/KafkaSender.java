package com.yash.experiments.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSender.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaSender(ObjectMapper objectMapper,KafkaTemplate<String, String> kafkaTemplate){
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic,Object obj){
        LOGGER.info(String.format("Message sending on topic %s -> message %s", topic,obj));
        String message;
        try{
            message = objectMapper.writeValueAsString(obj);
            LOGGER.info(message);
            var record = new ProducerRecord<String, String>(topic, message);
            record.headers().add("message-type",obj.getClass().getSimpleName().getBytes());
            kafkaTemplate.send(record);
        }catch(JsonProcessingException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
