package com.sportlink.moderation.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Only declares the topic we publish to. The 'account-events' topic we
 * consume from is owned (and created) by the Account Management Service.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${sportlink.kafka.topics.moderation-events}")
    private String moderationEventsTopic;

    @Bean
    public NewTopic moderationEventsTopic() {
        return TopicBuilder.name(moderationEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
