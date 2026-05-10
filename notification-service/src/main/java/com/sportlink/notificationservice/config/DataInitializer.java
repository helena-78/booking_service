package com.sportlink.notificationservice.config;

import com.sportlink.notificationservice.model.*;
import com.sportlink.notificationservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

// @Profile("!test") ensures this is never loaded by @WebMvcTest or any test that sets profile="test"
@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(NotificationRepository notificationRepo,
                               NotificationPreferenceRepository prefRepo,
                               DeviceTokenRepository deviceTokenRepo) {
        return args -> {
            if (notificationRepo.count() == 0) {
                UUID user1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
                UUID user2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

                notificationRepo.save(Notification.builder()
                        .recipientId(user1).type(NotificationType.FOLLOWER_UPDATE)
                        .channel(Channel.IN_APP).payload("{\"message\":\"User2 started following you\"}").read(false).build());
                notificationRepo.save(Notification.builder()
                        .recipientId(user1).type(NotificationType.ACTIVITY_REMINDER)
                        .channel(Channel.IN_APP).payload("{\"message\":\"Your activity starts in 1 hour\"}").read(true).build());

                prefRepo.save(NotificationPreference.builder()
                        .userId(user1).eventType(NotificationType.FOLLOWER_UPDATE)
                        .channel(Channel.IN_APP).enabled(true).build());
                prefRepo.save(NotificationPreference.builder()
                        .userId(user1).eventType(NotificationType.ACTIVITY_REMINDER)
                        .channel(Channel.EMAIL).enabled(false).build());

                deviceTokenRepo.save(DeviceToken.builder()
                        .userId(user2).token("sample-fcm-token-abc123").platform(Platform.ANDROID).build());
            }
        };
    }
}
