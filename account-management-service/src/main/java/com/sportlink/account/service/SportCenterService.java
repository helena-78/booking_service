package com.sportlink.account.service;

import com.sportlink.account.dto.request.RegisterFacilityRequest;
import com.sportlink.account.dto.request.UpdateFacilityRequest;
import com.sportlink.account.dto.response.FacilityProfileResponse;
import com.sportlink.account.model.FacilityProfile;
import com.sportlink.account.model.valueobject.FacilityContactInfo;
import com.sportlink.account.model.valueobject.FacilityLocation;
import com.sportlink.account.repository.FacilityProfileRepository;
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
public class SportCenterService {

    private final FacilityProfileRepository facilityProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public FacilityProfileResponse registerFacility(RegisterFacilityRequest request) {
        if (facilityProfileRepository.existsByContactInfoEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        FacilityContactInfo contactInfo = FacilityContactInfo.builder()
                .email(request.getEmail())
                .contactName(request.getContactName())
                .contactPhone(request.getContactPhone())
                .website(request.getWebsite())
                .build();

        FacilityLocation location = FacilityLocation.builder()
                .city(request.getCity())
                .district(request.getDistrict())
                .coordinates(request.getCoordinates())
                .build();

        FacilityProfile facility = FacilityProfile.builder()
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .sportTypes(request.getSportTypes())
                .contactInfo(contactInfo)
                .location(location)
                .build();

        FacilityProfile saved = facilityProfileRepository.save(facility);
        log.info("Facility registered: {}", saved.getCenterId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FacilityProfileResponse getFacilityById(UUID centerId) {
        FacilityProfile facility = facilityProfileRepository.findById(centerId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + centerId));
        return toResponse(facility);
    }

    @Transactional
    public FacilityProfileResponse updateFacility(UUID centerId, UpdateFacilityRequest request) {
        FacilityProfile facility = facilityProfileRepository.findById(centerId)
                .orElseThrow(() -> new ResourceNotFoundException("Facility not found: " + centerId));

        if (request.getName() != null) {
            facility.setName(request.getName());
        }
        if (request.getSportTypes() != null) {
            facility.setSportTypes(request.getSportTypes());
        }

        FacilityContactInfo contact = facility.getContactInfo() != null ? facility.getContactInfo() : new FacilityContactInfo();
        if (request.getContactName() != null) {
            contact.setContactName(request.getContactName());
        }
        if (request.getContactPhone() != null) {
            contact.setContactPhone(request.getContactPhone());
        }
        if (request.getWebsite() != null) {
            contact.setWebsite(request.getWebsite());
        }
        facility.setContactInfo(contact);

        FacilityLocation location = facility.getLocation() != null ? facility.getLocation() : new FacilityLocation();
        if (request.getCity() != null) {
            location.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            location.setDistrict(request.getDistrict());
        }
        if (request.getCoordinates() != null) {
            location.setCoordinates(request.getCoordinates());
        }
        facility.setLocation(location);

        FacilityProfile saved = facilityProfileRepository.save(facility);
        log.info("Facility updated: {}", saved.getCenterId());
        return toResponse(saved);
    }

    private FacilityProfileResponse toResponse(FacilityProfile facility) {
        FacilityContactInfo contact = facility.getContactInfo();
        FacilityLocation location = facility.getLocation();
        return FacilityProfileResponse.builder()
                .centerId(facility.getCenterId())
                .name(facility.getName())
                .email(contact != null ? contact.getEmail() : null)
                .sportTypes(facility.getSportTypes())
                .contactName(contact != null ? contact.getContactName() : null)
                .contactPhone(contact != null ? contact.getContactPhone() : null)
                .website(contact != null ? contact.getWebsite() : null)
                .city(location != null ? location.getCity() : null)
                .district(location != null ? location.getDistrict() : null)
                .coordinates(location != null ? location.getCoordinates() : null)
                .status(facility.getStatus())
                .createdAt(facility.getCreatedAt())
                .updatedAt(facility.getUpdatedAt())
                .build();
    }
}
