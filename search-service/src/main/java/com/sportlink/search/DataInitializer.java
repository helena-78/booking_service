package com.sportlink.search;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sportlink.search.model.EntityType;
import com.sportlink.search.model.SearchFilters;
import com.sportlink.search.model.SearchIndex;
import com.sportlink.search.repository.SearchIndexRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadSearchData(SearchIndexRepository repository) {
        return args -> {

            // ====================================================
            // FACILITIES - entityId matches sportCenterId used by the
            // Booking Service's seeded bookings (sc-100, sc-101).
            // This is what makes the integration with the Booking
            // Service produce meaningful results.
            // ====================================================
            SearchFilters facilityFilters1 = SearchFilters.builder()
                    .sportType("BASKETBALL")
                    .location("Tartu")
                    .facilityName("Tartu Sport Hall")
                    .build();

            SearchIndex facility1 = SearchIndex.builder()
                    .indexId("idx-fac-001")
                    .entityType(EntityType.FACILITY)
                    .entityId("sc-100")
                    .displayName("Tartu Sport Hall")
                    .keywords(List.of("basketball", "tartu", "indoor", "hall"))
                    .lastIndexedAt(LocalDateTime.now())
                    .filters(facilityFilters1)
                    .build();
            repository.save(facility1);

            SearchFilters facilityFilters2 = SearchFilters.builder()
                    .sportType("FOOTBALL")
                    .location("Tallinn")
                    .facilityName("Tallinn Football Arena")
                    .build();

            SearchIndex facility2 = SearchIndex.builder()
                    .indexId("idx-fac-002")
                    .entityType(EntityType.FACILITY)
                    .entityId("sc-101")
                    .displayName("Tallinn Football Arena")
                    .keywords(List.of("football", "tallinn", "outdoor", "arena"))
                    .lastIndexedAt(LocalDateTime.now())
                    .filters(facilityFilters2)
                    .build();
            repository.save(facility2);

            SearchFilters facilityFilters3 = SearchFilters.builder()
                    .sportType("BASKETBALL")
                    .location("Tallinn")
                    .facilityName("Kalev Sport Center")
                    .build();

            SearchIndex facility3 = SearchIndex.builder()
                    .indexId("idx-fac-003")
                    .entityType(EntityType.FACILITY)
                    .entityId("sc-200")
                    .displayName("Kalev Sport Center")
                    .keywords(List.of("basketball", "tallinn", "kalev"))
                    .lastIndexedAt(LocalDateTime.now())
                    .filters(facilityFilters3)
                    .build();
            repository.save(facility3);

            // ====================================================
            // USERS - used by the Matching Service to fetch candidates.
            // ====================================================
            SearchFilters userFilters1 = SearchFilters.builder()
                    .sportType("BASKETBALL")
                    .skillLevel("INTERMEDIATE")
                    .location("Tartu")
                    .userName("Olena K.")
                    .userRatingScore(4.6)
                    .userSportPreferences("BASKETBALL,VOLLEYBALL")
                    .build();

            SearchIndex user1 = SearchIndex.builder()
                    .indexId("idx-usr-001")
                    .entityType(EntityType.USER)
                    .entityId("usr-100")
                    .displayName("Olena K.")
                    .keywords(List.of("basketball", "tartu", "intermediate"))
                    .lastIndexedAt(LocalDateTime.now())
                    .filters(userFilters1)
                    .build();
            repository.save(user1);

            SearchFilters userFilters2 = SearchFilters.builder()
                    .sportType("FOOTBALL")
                    .skillLevel("ADVANCED")
                    .location("Tallinn")
                    .userName("Kirill M.")
                    .userRatingScore(4.9)
                    .userSportPreferences("FOOTBALL")
                    .build();

            SearchIndex user2 = SearchIndex.builder()
                    .indexId("idx-usr-002")
                    .entityType(EntityType.USER)
                    .entityId("usr-101")
                    .displayName("Kirill M.")
                    .keywords(List.of("football", "tallinn", "advanced"))
                    .lastIndexedAt(LocalDateTime.now())
                    .filters(userFilters2)
                    .build();
            repository.save(user2);

            // ====================================================
            // ACTIVITIES - indexed from Activity Management events.
            // ====================================================
            SearchFilters activityFilters1 = SearchFilters.builder()
                    .sportType("BASKETBALL")
                    .location("Tartu")
                    .activityTitle("Sunday Pickup Basketball")
                    .activityStatus("OPEN")
                    .activityMaxParticipants(10)
                    .activityTimeSlots("ts-300")
                    .build();

            SearchIndex activity1 = SearchIndex.builder()
                    .indexId("idx-act-001")
                    .entityType(EntityType.ACTIVITY)
                    .entityId("act-200")
                    .displayName("Sunday Pickup Basketball")
                    .keywords(List.of("basketball", "pickup", "sunday", "tartu"))
                    .lastIndexedAt(LocalDateTime.now())
                    .filters(activityFilters1)
                    .build();
            repository.save(activity1);

            SearchFilters activityFilters2 = SearchFilters.builder()
                    .sportType("FOOTBALL")
                    .location("Tallinn")
                    .activityTitle("Friday Night Football")
                    .activityStatus("OPEN")
                    .activityMaxParticipants(22)
                    .activityTimeSlots("ts-301")
                    .build();

            SearchIndex activity2 = SearchIndex.builder()
                    .indexId("idx-act-002")
                    .entityType(EntityType.ACTIVITY)
                    .entityId("act-201")
                    .displayName("Friday Night Football")
                    .keywords(List.of("football", "friday", "tallinn"))
                    .lastIndexedAt(LocalDateTime.now())
                    .filters(activityFilters2)
                    .build();
            repository.save(activity2);
        };
    }
}
