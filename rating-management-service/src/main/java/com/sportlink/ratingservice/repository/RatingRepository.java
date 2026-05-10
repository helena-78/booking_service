package com.sportlink.ratingservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportlink.ratingservice.model.Rating;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    List<Rating> findByReviewerId(UUID reviewerId);
    List<Rating> findByRevieweeId(UUID revieweeId);
    List<Rating> findByActivityId(UUID activityId);
    boolean existsByReviewerIdAndRevieweeIdAndActivityId(UUID reviewerId, UUID revieweeId, UUID activityId);
    List<Rating> findAllByRevieweeId(UUID revieweeId);
}
