package com.sportlink.interactionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlink.interactionservice.dto.request.FollowRequest;
import com.sportlink.interactionservice.dto.response.FollowResponse;
import com.sportlink.interactionservice.exception.AlreadyFollowingException;
import com.sportlink.interactionservice.exception.GlobalExceptionHandler;
import com.sportlink.interactionservice.exception.ResourceNotFoundException;
import com.sportlink.interactionservice.service.FollowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for FollowController using @WebMvcTest (only the web layer is loaded).
 *
 * Dependency under test: FollowService internally calls AccountManagementClient,
 * which makes a REST call to the Account Management Service to validate that the
 * target user exists. By mocking FollowService here, we verify the controller
 * behaves correctly based on what Account Management returns:
 *   - User found      → FollowService succeeds → controller returns 201
 *   - User not found  → FollowService throws ResourceNotFoundException → controller returns 404
 *   - Already follows → FollowService throws AlreadyFollowingException → controller returns 409
 */
@WebMvcTest(FollowController.class)
@Import(GlobalExceptionHandler.class)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FollowService followService;

    private static final UUID CURRENT_USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID TARGET_USER  = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // -----------------------------------------------------------------------
    // POST /api/follows — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/follows — 201 Created when Account Management confirms the target user exists")
    void followUser_happyPath_returns201() throws Exception {
        FollowResponse response = FollowResponse.builder()
                .followerId(CURRENT_USER)
                .followeeId(TARGET_USER)
                .createdAt(LocalDateTime.now())
                .build();

        // Simulates: Account Management returned user OK → FollowService created the relationship
        when(followService.followUser(CURRENT_USER, TARGET_USER)).thenReturn(response);

        mockMvc.perform(post("/api/follows")
                        .header("X-User-Id", CURRENT_USER.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest(TARGET_USER))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.followerId").value(CURRENT_USER.toString()))
                .andExpect(jsonPath("$.followeeId").value(TARGET_USER.toString()));
    }

    // -----------------------------------------------------------------------
    // POST /api/follows — error: target user not found in Account Management
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/follows — 404 when Account Management reports target user does not exist")
    void followUser_userNotFound_returns404() throws Exception {
        // Simulates: AccountManagementClient received 404 from Account Management Service →
        // FollowService throws ResourceNotFoundException → controller must return 404
        when(followService.followUser(any(UUID.class), any(UUID.class)))
                .thenThrow(new ResourceNotFoundException("User not found: " + TARGET_USER));

        mockMvc.perform(post("/api/follows")
                        .header("X-User-Id", CURRENT_USER.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest(TARGET_USER))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: " + TARGET_USER));
    }

    // -----------------------------------------------------------------------
    // POST /api/follows — error: duplicate follow attempt
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/follows — 409 Conflict when already following the target user")
    void followUser_alreadyFollowing_returns409() throws Exception {
        when(followService.followUser(any(UUID.class), any(UUID.class)))
                .thenThrow(new AlreadyFollowingException("Already following user " + TARGET_USER));

        mockMvc.perform(post("/api/follows")
                        .header("X-User-Id", CURRENT_USER.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest(TARGET_USER))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Already following user " + TARGET_USER));
    }

    // -----------------------------------------------------------------------
    // POST /api/follows — error: missing body field
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/follows — 400 Bad Request when targetUserId is missing")
    void followUser_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/follows")
                        .header("X-User-Id", CURRENT_USER.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // DELETE /api/follows/{targetUserId} — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/follows/{id} — 204 No Content on successful unfollow")
    void unfollowUser_happyPath_returns204() throws Exception {
        mockMvc.perform(delete("/api/follows/{id}", TARGET_USER)
                        .header("X-User-Id", CURRENT_USER.toString()))
                .andExpect(status().isNoContent());
    }

    // -----------------------------------------------------------------------
    // DELETE /api/follows/{targetUserId} — error: relationship does not exist
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/follows/{id} — 404 when follow relationship does not exist")
    void unfollowUser_notFound_returns404() throws Exception {
        // Simulates: user tries to unfollow someone they never followed
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Follow relationship not found"))
                .when(followService).unfollowUser(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/follows/{id}", TARGET_USER)
                        .header("X-User-Id", CURRENT_USER.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Follow relationship not found"));
    }
}
