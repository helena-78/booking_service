package com.sportlink.activitymanagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka topic creation
 * Spring will auto-create the topic on startup
 */
@Configuration
public class KafkaTopicConfiguration {

    public static final String ACTIVITY_CANCELLED_TOPIC = "activity-cancelled-events";

    @Bean
    public NewTopic activityCancelledTopic() {
        return TopicBuilder.name(ACTIVITY_CANCELLED_TOPIC).build();
    }
}