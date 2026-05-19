package com.sportlink.account.service;

import com.sportlink.account.dto.request.LoginRequest;
import com.sportlink.account.dto.response.AuthResponse;
import com.sportlink.account.exception.InvalidCredentialsException;
import com.sportlink.account.model.FacilityProfile;
import com.sportlink.account.model.UserProfile;
import com.sportlink.account.repository.FacilityProfileRepository;
import com.sportlink.account.repository.UserProfileRepository;
import com.sportlink.account.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String ACCOUNT_TYPE_USER = "USER";
    private static final String ACCOUNT_TYPE_FACILITY = "FACILITY";
    private static final String FACILITY_ROLE = "FACILITY";

    private final UserProfileRepository userProfileRepository;
    private final FacilityProfileRepository facilityProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Optional<UserProfile> userMatch = userProfileRepository.findByContactInfoEmail(request.getEmail());
        if (userMatch.isPresent()) {
            UserProfile user = userMatch.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            String role = user.getRole().name();
            String token = tokenProvider.generateToken(user.getUserId(), ACCOUNT_TYPE_USER, role);
            log.info("User login: {} (role: {})", user.getUserId(), role);
            return AuthResponse.builder()
                    .accountId(user.getUserId())
                    .accountType(ACCOUNT_TYPE_USER)
                    .role(role)
                    .token(token)
                    .build();
        }

        Optional<FacilityProfile> facilityMatch = facilityProfileRepository.findByContactInfoEmail(request.getEmail());
        if (facilityMatch.isPresent()) {
            FacilityProfile facility = facilityMatch.get();
            if (!passwordEncoder.matches(request.getPassword(), facility.getPasswordHash())) {
                throw new InvalidCredentialsException("Invalid email or password");
            }
            String token = tokenProvider.generateToken(facility.getCenterId(), ACCOUNT_TYPE_FACILITY, FACILITY_ROLE);
            log.info("Facility login: {}", facility.getCenterId());
            return AuthResponse.builder()
                    .accountId(facility.getCenterId())
                    .accountType(ACCOUNT_TYPE_FACILITY)
                    .role(FACILITY_ROLE)
                    .token(token)
                    .build();
        }

        throw new InvalidCredentialsException("Invalid email or password");
    }
}
