package com.sportlink.ratingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlink.ratingservice.dto.RatingRequest;
import com.sportlink.ratingservice.model.Rating;
import com.sportlink.ratingservice.service.RatingService;
import com.sportlink.ratingservice.controller.RatingController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatingController.class)
class RatingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RatingService ratingService;  // mocked — this internally calls the stubs

    private RatingRequest validRequest;
    private UUID reviewerId;
    private UUID revieweeId;
    private UUID activityId;

    @BeforeEach
    void setUp() {
        reviewerId = UUID.randomUUID();
        revieweeId = UUID.randomUUID();
        activityId = UUID.randomUUID();

        validRequest = new RatingRequest();
        validRequest.setReviewerId(reviewerId);
        validRequest.setRevieweeId(revieweeId);
        validRequest.setActivityId(activityId);
        validRequest.setBehaviorValue(4);
        validRequest.setBehaviorLabel("FAIR_PLAY");
        validRequest.setSkillValue(5);
        validRequest.setSkillLabel("ADVANCED");
    }

    // --- HAPPY PATH ---
    // POST /api/ratings — mocked service returns a saved Rating
    // This endpoint internally depends on AccountManagementStub and ActivityManagementStub
    // via RatingService. The mock simulates a successful response from those components.
    @Test
    void submitRating_whenDependenciesReturnSuccess_returns201() throws Exception {
        Rating savedRating = Rating.builder()
                .ratingId(UUID.randomUUID())
                .reviewerId(reviewerId)
                .revieweeId(revieweeId)
                .activityId(activityId)
                .behaviorValue(4)
                .behaviorLabel("FAIR_PLAY")
                .skillValue(5)
                .skillLabel("ADVANCED")
                .build();

        // Simulates: AccountManagementStub said both users exist,
        // ActivityManagementStub said activity is COMPLETED and both participated
        when(ratingService.submitRating(any(RatingRequest.class))).thenReturn(savedRating);

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ratingId").isNotEmpty())
                .andExpect(jsonPath("$.behaviorValue").value(4))
                .andExpect(jsonPath("$.skillLabel").value("ADVANCED"));
    }

    // --- ERROR CASE ---
    // POST /api/ratings — mocked service throws because the activity stub
    // returned that the activity is not COMPLETED / users didn't participate
    @Test
    void submitRating_whenActivityValidationFails_returns400() throws Exception {
        // Simulates: ActivityManagementStub returned false — activity not COMPLETED
        when(ratingService.submitRating(any(RatingRequest.class)))
                .thenThrow(new IllegalStateException("Both users must have participated in a COMPLETED activity."));

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- ERROR CASE ---
    // POST /api/ratings — mocked service throws because AccountManagementStub
    // said the reviewer doesn't exist
    @Test
    void submitRating_whenReviewerNotFound_returns400() throws Exception {
        // Simulates: AccountManagementStub returned false — reviewer doesn't exist
        when(ratingService.submitRating(any(RatingRequest.class)))
                .thenThrow(new IllegalArgumentException("Reviewer not found: " + reviewerId));

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Reviewer not found: " + reviewerId));
    }
}