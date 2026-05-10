package com.sportlink.notificationservice.service;

import com.sportlink.notificationservice.dto.request.PreferenceUpdateRequest;
import com.sportlink.notificationservice.dto.response.PreferenceResponse;
import com.sportlink.notificationservice.model.NotificationPreference;
import com.sportlink.notificationservice.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Transactional(readOnly = true)
    public List<PreferenceResponse> getPreferences(UUID userId) {
        return preferenceRepository.findByUserId(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public PreferenceResponse updatePreference(UUID userId, PreferenceUpdateRequest request) {
        NotificationPreference pref = preferenceRepository
                .findByUserIdAndEventTypeAndChannel(userId, request.getEventType(), request.getChannel())
                .orElseGet(() -> NotificationPreference.builder()
                        .userId(userId)
                        .eventType(request.getEventType())
                        .channel(request.getChannel())
                        .build());
        pref.setEnabled(request.getEnabled());
        return toResponse(preferenceRepository.save(pref));
    }

    private PreferenceResponse toResponse(NotificationPreference p) {
        return PreferenceResponse.builder()
                .id(p.getId())
                .eventType(p.getEventType())
                .channel(p.getChannel())
                .enabled(p.isEnabled())
                .build();
    }
}
