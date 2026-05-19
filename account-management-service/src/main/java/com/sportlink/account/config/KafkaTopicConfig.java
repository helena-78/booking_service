package com.sportlink.account.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * KafkaAdmin auto-creates topics declared as NewTopic beans on startup,
 * so the producer doesn't fail on first send.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${sportlink.kafka.topics.account-events}")
    private String accountEventsTopic;

    @Bean
    public NewTopic accountEventsTopic() {
        return TopicBuilder.name(accountEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
