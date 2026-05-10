package com.sportlink.interactionservice.repository;

import com.sportlink.interactionservice.model.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    Page<Follow> findByFolloweeId(UUID followeeId, Pageable pageable);
    Page<Follow> findByFollowerId(UUID followerId, Pageable pageable);
    Optional<Follow> findByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    long countByFolloweeId(UUID followeeId);
    long countByFollowerId(UUID followerId);
}
