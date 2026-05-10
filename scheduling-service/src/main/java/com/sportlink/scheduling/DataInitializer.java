package com.sportlink.scheduling;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sportlink.scheduling.model.TimeSlot;
import com.sportlink.scheduling.model.TimeSlotStatus;
import com.sportlink.scheduling.repository.TimeSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TimeSlotRepository timeSlotRepository;

    @Override
    public void run(String... args) {
        if (timeSlotRepository.count() > 0) {
            return; // already seeded
        }

        UUID demoFacilityOrganizer = UUID.fromString("33333333-3333-3333-3333-333333333333");
        LocalDateTime base = LocalDateTime.now()
                .plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        // Three sample available slots on the next day.
        for (int i = 0; i < 3; i++) {
            LocalDateTime start = base.plusHours(i * 2);
            TimeSlot slot = TimeSlot.builder()
                    .slotId(UUID.randomUUID())
                    .startTime(start)
                    .endTime(start.plusHours(1))
                    .status(TimeSlotStatus.AVAILABLE)
                    .organizerId(demoFacilityOrganizer)
                    .build();
            timeSlotRepository.save(slot);
        }

        log.info("Seeded 3 AVAILABLE demo time slots starting at {}", base);
    }
}