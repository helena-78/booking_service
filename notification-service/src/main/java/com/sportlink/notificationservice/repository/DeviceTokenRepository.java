package com.sportlink.notificationservice.repository;

import com.sportlink.notificationservice.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    List<DeviceToken> findByUserId(UUID userId);
    Optional<DeviceToken> findByToken(String token);
    boolean existsByToken(String token);
}
