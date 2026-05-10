package com.sportlink.notificationservice.repository;

import com.sportlink.notificationservice.model.UserProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProjectionRepository extends JpaRepository<UserProjection, UUID> {
    Optional<UserProjection> findByUserId(UUID userId);
}
