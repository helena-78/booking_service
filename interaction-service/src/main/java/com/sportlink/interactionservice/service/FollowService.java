package com.sportlink.interactionservice.service;

import com.sportlink.interactionservice.client.AccountManagementClient;
import com.sportlink.interactionservice.config.RabbitMQConfig;
import com.sportlink.interactionservice.dto.response.FollowResponse;
import com.sportlink.interactionservice.dto.response.UserSummaryResponse;
import com.sportlink.interactionservice.event.UserFollowedEvent;
import com.sportlink.interactionservice.exception.AlreadyFollowingException;
import com.sportlink.interactionservice.exception.ResourceNotFoundException;
import com.sportlink.interactionservice.model.Follow;
import com.sportlink.interactionservice.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final AccountManagementClient accountManagementClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public FollowResponse followUser(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("A user cannot follow themselves");
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new AlreadyFollowingException("Already following user " + followeeId);
        }

        // Validate target user exists via Account Management Service (circuit-breaker protected)
        accountManagementClient.getUserById(followeeId);

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followeeId(followeeId)
                .build();
        Follow saved = followRepository.save(follow);

        publishEvent(new UserFollowedEvent(followerId, followeeId, LocalDateTime.now()),
                RabbitMQConfig.USER_FOLLOWED_ROUTING_KEY);

        return toFollowResponse(saved);
    }

    @Transactional
    public void unfollowUser(UUID followerId, UUID followeeId) {
        Follow follow = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Follow relationship not found between " + followerId + " and " + followeeId));
        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getFollowers(UUID userId, Pageable pageable) {
        return followRepository.findByFolloweeId(userId, pageable)
                .map(f -> UserSummaryResponse.builder()
                        .userId(f.getFollowerId())
                        .username(null)
                        .build());
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getFollowing(UUID userId, Pageable pageable) {
        return followRepository.findByFollowerId(userId, pageable)
                .map(f -> UserSummaryResponse.builder()
                        .userId(f.getFolloweeId())
                        .username(null)
                        .build());
    }

    private FollowResponse toFollowResponse(Follow f) {
        return FollowResponse.builder()
                .followerId(f.getFollowerId())
                .followeeId(f.getFolloweeId())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private void publishEvent(Object event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info("Published event [{}] with routing key [{}]", event.getClass().getSimpleName(), routingKey);
        } catch (Exception ex) {
            log.warn("Failed to publish event to RabbitMQ (service continues): {}", ex.getMessage());
        }
    }
}
