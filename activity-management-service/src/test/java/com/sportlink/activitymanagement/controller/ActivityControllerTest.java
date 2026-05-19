package com.sportlink.activitymanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlink.activitymanagement.client.AccountManagementClient;
import com.sportlink.activitymanagement.dto.CreateActivityRequest;
import com.sportlink.activitymanagement.dto.UserDto;
import com.sportlink.activitymanagement.exception.UserNotValidException;
import com.sportlink.activitymanagement.repository.ActivityRepository;
import com.sportlink.activitymanagement.repository.ParticipantRepository;
import com.sportlink.activitymanagement.service.ActivityService;

/**
 * Integration test for the create-activity endpoint.
 *    one happy path test
 *    one error case test
 *    tested endpoint depends on another component (AccountManagementClient) that is mocked
 */
@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ActivityRepository activityRepository;
    @MockBean
    private ParticipantRepository participantRepository;

    @MockBean
    private AccountManagementClient accountManagementClient;

    @MockBean
    private com.sportlink.activitymanagement.client.SchedulingClient schedulingClient;

    @MockBean
    private com.sportlink.activitymanagement.service.ActivityEventPublisher activityEventPublisher;

 
    @TestConfiguration
    static class TestConfig {
        @Bean
        ActivityService activityService(ActivityRepository ar,
                                        ParticipantRepository pr,
                                        AccountManagementClient amc,
                                        com.sportlink.activitymanagement.client.SchedulingClient sc,
                                        com.sportlink.activitymanagement.service.ActivityEventPublisher aep) {
            return new ActivityService(ar, pr, amc, sc, aep);
        }
    }

    @Test
    void createActivity_happyPath_returns201() throws Exception {
        UUID organizerId = UUID.randomUUID();

        when(accountManagementClient.validateActiveUser(organizerId))
                .thenReturn(UserDto.builder()
                        .userId(organizerId)
                        .name("Test Organizer")
                        .status("ACTIVE")
                        .role("USER")
                        .build());

        when(activityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateActivityRequest request = CreateActivityRequest.builder()
                .organizerId(organizerId)
                .title("Saturday Football")
                .sportType("football")
                .maxParticipants(10)
                .description("Weekly pickup game")
                .build();

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Saturday Football"))
                .andExpect(jsonPath("$.sportType").value("football"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.organizerId").value(organizerId.toString()))
                // Organizer is auto-added as the first participant.
                .andExpect(jsonPath("$.participants.length()").value(1))
                .andExpect(jsonPath("$.participants[0].role").value("ORGANIZER"));
    }

    @Test
    void createActivity_whenAccountManagementRejectsUser_returns400() throws Exception {
        UUID organizerId = UUID.randomUUID();

        // Account Management says: user is suspended → service throws.
        when(accountManagementClient.validateActiveUser(organizerId))
                .thenThrow(new UserNotValidException(
                        "User " + organizerId + " is not active (status=SUSPENDED)"));

        CreateActivityRequest request = CreateActivityRequest.builder()
                .organizerId(organizerId)
                .title("Friday Tennis")
                .sportType("tennis")
                .maxParticipants(4)
                .build();

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("not active")));
    }
}