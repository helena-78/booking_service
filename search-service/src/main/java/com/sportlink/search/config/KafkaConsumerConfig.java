package com.sportlink.search.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.sportlink.search.event.AccountEvent;
import com.sportlink.search.event.ActivityEvent;
import com.sportlink.search.event.BookingEvent;
import com.sportlink.search.event.ModerationEvent;

@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    public KafkaConsumerConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    // -----------------------------------------------------------------
    // Booking events (booking-events topic)
    // -----------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, BookingEvent> bookingEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                jsonDeserializer(BookingEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingEvent>
            bookingEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BookingEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(bookingEventConsumerFactory());
        return factory;
    }

    // -----------------------------------------------------------------
    // Activity events (activity-events topic)
    // -----------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, ActivityEvent> activityEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                jsonDeserializer(ActivityEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ActivityEvent>
            activityEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ActivityEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(activityEventConsumerFactory());
        return factory;
    }

    // -----------------------------------------------------------------
    // Account events (account-events topic)
    // -----------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, AccountEvent> accountEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                jsonDeserializer(AccountEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountEvent>
            accountEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AccountEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(accountEventConsumerFactory());
        return factory;
    }

    // -----------------------------------------------------------------
    // Moderation events (moderation-events topic)
    // -----------------------------------------------------------------

    @Bean
    public ConsumerFactory<String, ModerationEvent> moderationEventConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerProps(),
                new StringDeserializer(),
                jsonDeserializer(ModerationEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ModerationEvent>
            moderationEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ModerationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(moderationEventConsumerFactory());
        return factory;
    }

    // -----------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------

    private Map<String, Object> consumerProps() {
        // Pull defaults from spring.kafka.* and then enforce the values this
        // service needs (group id, bootstrap servers, auto-offset).
        Map<String, Object> props = new HashMap<>(
                kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        props.keySet().removeIf(k -> k != null && k.startsWith("spring.json."));
        return props;
    }

    private <T> JsonDeserializer<T> jsonDeserializer(Class<T> type) {
        JsonDeserializer<T> d = new JsonDeserializer<>(type);
        d.ignoreTypeHeaders();
        d.addTrustedPackages("*");
        return d;
    }
}
