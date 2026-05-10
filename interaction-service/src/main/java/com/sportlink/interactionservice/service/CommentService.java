package com.sportlink.interactionservice.service;

import com.sportlink.interactionservice.config.RabbitMQConfig;
import com.sportlink.interactionservice.dto.request.CommentRequest;
import com.sportlink.interactionservice.dto.response.CommentResponse;
import com.sportlink.interactionservice.event.CommentPostedEvent;
import com.sportlink.interactionservice.exception.ForbiddenException;
import com.sportlink.interactionservice.exception.ResourceNotFoundException;
import com.sportlink.interactionservice.model.Comment;
import com.sportlink.interactionservice.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public CommentResponse postComment(UUID authorId, UUID activityId, CommentRequest request) {
        Comment comment = Comment.builder()
                .activityId(activityId)
                .authorId(authorId)
                .body(request.getBody())
                .build();
        Comment saved = commentRepository.save(comment);

        publishEvent(
                new CommentPostedEvent(saved.getId(), activityId, authorId,
                        request.getActivityOwnerId(), saved.getBody(), LocalDateTime.now()),
                RabbitMQConfig.COMMENT_POSTED_ROUTING_KEY
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(UUID activityId, Pageable pageable) {
        return commentRepository.findByActivityIdOrderByCreatedAtAsc(activityId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CommentResponse editComment(UUID callerId, UUID commentId, CommentRequest request) {
        Comment comment = findOrThrow(commentId);
        if (!comment.getAuthorId().equals(callerId)) {
            throw new ForbiddenException("Only the comment author can edit this comment");
        }
        comment.setBody(request.getBody());
        return toResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(UUID callerId, String callerRole, UUID commentId) {
        Comment comment = findOrThrow(commentId);
        boolean isAuthor = comment.getAuthorId().equals(callerId);
        boolean isModerator = "MODERATOR".equalsIgnoreCase(callerRole);
        if (!isAuthor && !isModerator) {
            throw new ForbiddenException("Only the comment author or a moderator can delete this comment");
        }
        commentRepository.delete(comment);
    }

    private Comment findOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }

    private CommentResponse toResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .activityId(c.getActivityId())
                .authorId(c.getAuthorId())
                .body(c.getBody())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
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
