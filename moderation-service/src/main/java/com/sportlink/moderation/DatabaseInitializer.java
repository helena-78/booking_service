package com.sportlink.moderation;

import com.sportlink.moderation.model.ModerationCase;
import com.sportlink.moderation.model.enums.ContentType;
import com.sportlink.moderation.model.enums.Verdict;
import com.sportlink.moderation.repository.ModerationCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer implements CommandLineRunner {

    private final ModerationCaseRepository caseRepository;

    @Override
    public void run(String... args) {
        if (caseRepository.count() > 0) {
            log.info("Skipping seed data, moderation_cases already populated");
            return;
        }

        ModerationCase pendingCommentCase = ModerationCase.builder()
                .reportedContentId(UUID.randomUUID())
                .contentType(ContentType.COMMENT)
                .reportedByUserId(UUID.randomUUID())
                .targetUserId(UUID.randomUUID())
                .content("This comment uses offensive language toward another player.")
                .verdict(Verdict.PENDING)
                .build();

        ModerationCase dismissedCase = ModerationCase.builder()
                .reportedContentId(UUID.randomUUID())
                .contentType(ContentType.ACTIVITY_DESCRIPTION)
                .reportedByUserId(UUID.randomUUID())
                .targetUserId(UUID.randomUUID())
                .content("Reporter claims the activity description is misleading.")
                .verdict(Verdict.DISMISSED)
                .moderatorId(UUID.randomUUID())
                .resolvedAt(LocalDateTime.now().minusDays(2))
                .build();

        ModerationCase removedContentCase = ModerationCase.builder()
                .reportedContentId(UUID.randomUUID())
                .contentType(ContentType.USER_PROFILE)
                .reportedByUserId(UUID.randomUUID())
                .targetUserId(UUID.randomUUID())
                .content("Profile contains inappropriate biographical text.")
                .verdict(Verdict.REMOVE_CONTENT)
                .moderatorId(UUID.randomUUID())
                .resolvedAt(LocalDateTime.now().minusDays(1))
                .build();

        caseRepository.save(pendingCommentCase);
        caseRepository.save(dismissedCase);
        caseRepository.save(removedContentCase);

        log.info("Seeded {} moderation cases", caseRepository.count());
    }
}
