package com.sportlink.notificationservice.service;

import com.sportlink.notificationservice.dto.request.DeviceTokenRequest;
import com.sportlink.notificationservice.dto.response.DeviceTokenResponse;
import com.sportlink.notificationservice.exception.ResourceNotFoundException;
import com.sportlink.notificationservice.model.DeviceToken;
import com.sportlink.notificationservice.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public DeviceTokenResponse register(UUID userId, DeviceTokenRequest request) {
        DeviceToken token = deviceTokenRepository.findByToken(request.getToken())
                .orElseGet(() -> DeviceToken.builder()
                        .userId(userId)
                        .token(request.getToken())
                        .platform(request.getPlatform())
                        .build());
        token.setUserId(userId);
        token.setPlatform(request.getPlatform());
        DeviceToken saved = deviceTokenRepository.save(token);
        return toResponse(saved);
    }

    @Transactional
    public void deregister(UUID userId, String tokenValue) {
        DeviceToken token = deviceTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResourceNotFoundException("Device token not found: " + tokenValue));
        if (!token.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Device token not found: " + tokenValue);
        }
        deviceTokenRepository.delete(token);
    }

    private DeviceTokenResponse toResponse(DeviceToken t) {
        return DeviceTokenResponse.builder()
                .id(t.getId())
                .token(t.getToken())
                .platform(t.getPlatform())
                .registeredAt(t.getRegisteredAt())
                .build();
    }
}
