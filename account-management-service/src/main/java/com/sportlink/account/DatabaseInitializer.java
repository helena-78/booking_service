package com.sportlink.account;

import com.sportlink.account.model.FacilityProfile;
import com.sportlink.account.model.UserProfile;
import com.sportlink.account.model.enums.FacilityStatus;
import com.sportlink.account.model.enums.SkillLevel;
import com.sportlink.account.model.enums.UserRole;
import com.sportlink.account.model.enums.UserStatus;
import com.sportlink.account.model.valueobject.FacilityContactInfo;
import com.sportlink.account.model.valueobject.FacilityLocation;
import com.sportlink.account.model.valueobject.UserContactInfo;
import com.sportlink.account.model.valueobject.UserLocation;
import com.sportlink.account.repository.FacilityProfileRepository;
import com.sportlink.account.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final UserProfileRepository userProfileRepository;
    private final FacilityProfileRepository facilityProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userProfileRepository.count() > 0 || facilityProfileRepository.count() > 0) {
            log.info("Skipping seed data, database already populated");
            return;
        }

        UserProfile demoUser = UserProfile.builder()
                .name("Demo User")
                .contactInfo(UserContactInfo.builder()
                        .email("demo@sportlink.test")
                        .phone("+372 555 0100")
                        .build())
                .passwordHash(passwordEncoder.encode("demoPassword1"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .skillLevel(SkillLevel.INTERMEDIATE)
                .language("en")
                .sportPreferences("badminton,tennis")
                .location(UserLocation.builder().city("Tartu").district("Annelinn").build())
                .build();

        UserProfile demoOrganizer = UserProfile.builder()
                .name("Demo Organizer")
                .contactInfo(UserContactInfo.builder()
                        .email("organizer@sportlink.test")
                        .build())
                .passwordHash(passwordEncoder.encode("organizerPass1"))
                .role(UserRole.ORGANIZER)
                .status(UserStatus.ACTIVE)
                .skillLevel(SkillLevel.ADVANCED)
                .location(UserLocation.builder().city("Tallinn").build())
                .build();

        UserProfile demoModerator = UserProfile.builder()
                .name("Demo Moderator")
                .contactInfo(UserContactInfo.builder()
                        .email("moderator@sportlink.test")
                        .build())
                .passwordHash(passwordEncoder.encode("moderatorPass1"))
                .role(UserRole.MODERATOR)
                .status(UserStatus.ACTIVE)
                .location(UserLocation.builder().city("Tartu").build())
                .build();

        FacilityProfile demoFacility = FacilityProfile.builder()
                .name("Tartu Sports Hall")
                .passwordHash(passwordEncoder.encode("facilityPass1"))
                .sportTypes("badminton,basketball,volleyball")
                .contactInfo(FacilityContactInfo.builder()
                        .email("info@tartusportshall.test")
                        .contactName("Maria Tamm")
                        .contactPhone("+372 555 0200")
                        .website("https://tartusportshall.test")
                        .build())
                .location(FacilityLocation.builder()
                        .city("Tartu")
                        .district("Kesklinn")
                        .coordinates("58.3776,26.7290")
                        .build())
                .status(FacilityStatus.ACTIVE)
                .build();

        userProfileRepository.save(demoUser);
        userProfileRepository.save(demoOrganizer);
        userProfileRepository.save(demoModerator);
        facilityProfileRepository.save(demoFacility);

        log.info("Seeded {} users and {} facilities",
                userProfileRepository.count(), facilityProfileRepository.count());
    }
}
