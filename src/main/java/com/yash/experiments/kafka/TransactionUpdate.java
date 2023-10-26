package com.yash.experiments.kafka;

import com.yash.experiments.constants.KafkaConstants;
import com.yash.experiments.events.TxnUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionUpdate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionUpdate.class);

    @KafkaListener(topics = KafkaConstants.TXN_UPDATE_TOPIC,
            groupId = KafkaConstants.UPDATE_TXN_CONSUMER_GROUP_ID)
    public void consume(TxnUpdateEvent message){
        LOGGER.info(String.format("received update event -> %s", message));
        LOGGER.info("good good done!");
    }
}
