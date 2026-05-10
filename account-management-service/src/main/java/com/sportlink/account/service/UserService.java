package com.sportlink.account.service;

import com.sportlink.account.dto.request.RegisterUserRequest;
import com.sportlink.account.dto.request.UpdateUserRequest;
import com.sportlink.account.dto.request.UpdateUserStatusRequest;
import com.sportlink.account.dto.response.UserProfileResponse;
import com.sportlink.account.model.UserProfile;
import com.sportlink.account.model.enums.UserRole;
import com.sportlink.account.model.valueobject.UserContactInfo;
import com.sportlink.account.model.valueobject.UserLocation;
import com.sportlink.account.repository.UserProfileRepository;
import com.sportlink.account.exception.EmailAlreadyExistsException;
import com.sportlink.account.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserProfileResponse registerUser(RegisterUserRequest request) {
        if (userProfileRepository.existsByContactInfoEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        UserContactInfo contactInfo = UserContactInfo.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .other(request.getOther())
                .build();

        UserLocation location = UserLocation.builder()
                .city(request.getCity())
                .district(request.getDistrict())
                .build();

        UserProfile user = UserProfile.builder()
                .name(request.getName())
                .contactInfo(contactInfo)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.USER)
                .skillLevel(request.getSkillLevel())
                .language(request.getLanguage())
                .sportPreferences(request.getSportPreferences())
                .location(location)
                .availabilityId(request.getAvailabilityId())
                .build();

        UserProfile saved = userProfileRepository.save(user);
        log.info("User registered: {}", saved.getUserId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(UUID userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateUser(UUID userId, UpdateUserRequest request) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getSkillLevel() != null) {
            user.setSkillLevel(request.getSkillLevel());
        }
        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }
        if (request.getSportPreferences() != null) {
            user.setSportPreferences(request.getSportPreferences());
        }

        UserContactInfo contact = user.getContactInfo() != null ? user.getContactInfo() : new UserContactInfo();
        if (request.getPhone() != null) {
            contact.setPhone(request.getPhone());
        }
        if (request.getOther() != null) {
            contact.setOther(request.getOther());
        }
        user.setContactInfo(contact);

        UserLocation location = user.getLocation() != null ? user.getLocation() : new UserLocation();
        if (request.getCity() != null) {
            location.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            location.setDistrict(request.getDistrict());
        }
        user.setLocation(location);

        UserProfile saved = userProfileRepository.save(user);
        log.info("User updated: {}", saved.getUserId());
        return toResponse(saved);
    }

    @Transactional
    public UserProfileResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setStatus(request.getStatus());
        UserProfile saved = userProfileRepository.save(user);
        log.info("User status changed: {} -> {}", saved.getUserId(), saved.getStatus());
        return toResponse(saved);
    }

    private UserProfileResponse toResponse(UserProfile user) {
        UserContactInfo contact = user.getContactInfo();
        UserLocation location = user.getLocation();
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(contact != null ? contact.getEmail() : null)
                .phone(contact != null ? contact.getPhone() : null)
                .other(contact != null ? contact.getOther() : null)
                .role(user.getRole())
                .status(user.getStatus())
                .skillLevel(user.getSkillLevel())
                .language(user.getLanguage())
                .sportPreferences(user.getSportPreferences())
                .city(location != null ? location.getCity() : null)
                .district(location != null ? location.getDistrict() : null)
                .availabilityId(user.getAvailabilityId())
                .behaviorLabel(user.getLabel() != null ? user.getLabel().getBehaviorLabel() : null)
                .labelValue(user.getLabel() != null ? user.getLabel().getLabelValue() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
