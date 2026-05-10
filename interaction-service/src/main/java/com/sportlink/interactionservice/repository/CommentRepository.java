package com.sportlink.interactionservice.repository;

import com.sportlink.interactionservice.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByActivityIdOrderByCreatedAtAsc(UUID activityId, Pageable pageable);
}
