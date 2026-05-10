package com.sportlink.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Main exchange (published by Interaction Service) ──────────────────
    public static final String INTERACTION_EXCHANGE = "interaction-events";

    // ── Dead Letter Exchange (receives nack'd / expired messages) ─────────
    public static final String DLX = "interaction-events.dlx";

    // ── Main queue names ───────────────────────────────────────────────────
    public static final String USER_FOLLOWED_QUEUE  = "notification.user.followed";
    public static final String COMMENT_POSTED_QUEUE = "notification.comment.posted";

    // ── Dead Letter Queue names ────────────────────────────────────────────
    public static final String USER_FOLLOWED_DLQ  = "notification.user.followed.dlq";
    public static final String COMMENT_POSTED_DLQ = "notification.comment.posted.dlq";

    // ── Routing keys (must match what Interaction Service publishes) ───────
    public static final String USER_FOLLOWED_KEY  = "user.followed";
    public static final String COMMENT_POSTED_KEY = "comment.posted";

    // ── Exchanges ──────────────────────────────────────────────────────────

    @Bean
    public TopicExchange interactionExchange() {
        return new TopicExchange(INTERACTION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    // ── Main queues — declared with DLX so failed messages are routed there

    @Bean
    public Queue userFollowedQueue() {
        return QueueBuilder.durable(USER_FOLLOWED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", USER_FOLLOWED_DLQ)
                .build();
    }

    @Bean
    public Queue commentPostedQueue() {
        return QueueBuilder.durable(COMMENT_POSTED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", COMMENT_POSTED_DLQ)
                .build();
    }

    // ── Dead Letter Queues (hold unprocessable messages for inspection) ────

    @Bean
    public Queue userFollowedDlq() {
        return QueueBuilder.durable(USER_FOLLOWED_DLQ).build();
    }

    @Bean
    public Queue commentPostedDlq() {
        return QueueBuilder.durable(COMMENT_POSTED_DLQ).build();
    }

    // ── Bindings ───────────────────────────────────────────────────────────

    @Bean
    public Binding userFollowedBinding(Queue userFollowedQueue, TopicExchange interactionExchange) {
        return BindingBuilder.bind(userFollowedQueue).to(interactionExchange).with(USER_FOLLOWED_KEY);
    }

    @Bean
    public Binding commentPostedBinding(Queue commentPostedQueue, TopicExchange interactionExchange) {
        return BindingBuilder.bind(commentPostedQueue).to(interactionExchange).with(COMMENT_POSTED_KEY);
    }

    @Bean
    public Binding userFollowedDlqBinding(Queue userFollowedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(userFollowedDlq).to(deadLetterExchange).with(USER_FOLLOWED_DLQ);
    }

    @Bean
    public Binding commentPostedDlqBinding(Queue commentPostedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(commentPostedDlq).to(deadLetterExchange).with(COMMENT_POSTED_DLQ);
    }

    // ── Message converter ──────────────────────────────────────────────────

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }

    // ── Listener container factory ─────────────────────────────────────────
    // defaultRequeueRejected=false: on exception, message is nack'd → goes to DLQ
    // instead of being requeued and retried forever.

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cf);
        factory.setMessageConverter(converter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
