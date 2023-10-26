package com.yash.experiments.kafka;


import com.yash.experiments.constants.KafkaConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic initTransactionTopic(){
        return TopicBuilder.name(KafkaConstants.RAISE_TXN_TOPIC)
                .partitions(3)
                .replicas(2)
                .build();
    }

    @Bean
    public NewTopic txnUpdateTopic(){
        return TopicBuilder.name(KafkaConstants.TXN_UPDATE_TOPIC)
                .partitions(4)
                .replicas(3)
                .build();
    }
}
