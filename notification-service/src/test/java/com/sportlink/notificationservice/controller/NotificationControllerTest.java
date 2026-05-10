package com.sportlink.notificationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlink.notificationservice.dto.request.DispatchNotificationRequest;
import com.sportlink.notificationservice.dto.response.NotificationResponse;
import com.sportlink.notificationservice.exception.GlobalExceptionHandler;
import com.sportlink.notificationservice.exception.ResourceNotFoundException;
import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.NotificationType;
import com.sportlink.notificationservice.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NotificationController using @WebMvcTest.
 *
 * Dependency under test: NotificationService depends on NotificationRepository
 * (persistence layer) and is the integration point between the controller and
 * the database. By mocking NotificationService, we verify the controller
 * correctly maps HTTP requests/responses without needing a real database.
 */
@WebMvcTest(NotificationController.class)
@Import(GlobalExceptionHandler.class)
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private NotificationService notificationService;

    private static final UUID USER_ID  = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID NOTIF_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // -----------------------------------------------------------------------
    // POST /api/notifications — happy path (internal dispatch)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/notifications — 201 Created on successful dispatch")
    void dispatch_happyPath_returns201() throws Exception {
        DispatchNotificationRequest req = new DispatchNotificationRequest(
                USER_ID, NotificationType.FOLLOWER_UPDATE, Channel.IN_APP,
                "{\"message\":\"User X followed you\"}");

        NotificationResponse response = NotificationResponse.builder()
                .id(NOTIF_ID).recipientId(USER_ID)
                .type(NotificationType.FOLLOWER_UPDATE).channel(Channel.IN_APP)
                .payload("{\"message\":\"User X followed you\"}").read(false)
                .createdAt(LocalDateTime.now()).build();

        when(notificationService.dispatch(any(DispatchNotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(NOTIF_ID.toString()))
                .andExpect(jsonPath("$.type").value("FOLLOWER_UPDATE"))
                .andExpect(jsonPath("$.read").value(false));
    }

    // -----------------------------------------------------------------------
    // POST — validation error (missing required fields)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/notifications — 400 when required fields missing")
    void dispatch_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // GET /api/notifications — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/notifications — 200 with paginated inbox")
    void getInbox_returnsPage() throws Exception {
        NotificationResponse notif = NotificationResponse.builder()
                .id(NOTIF_ID).recipientId(USER_ID)
                .type(NotificationType.FOLLOWER_UPDATE).channel(Channel.IN_APP)
                .payload("{\"message\":\"test\"}").read(false)
                .createdAt(LocalDateTime.now()).build();

        when(notificationService.getInbox(eq(USER_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notif)));

        mockMvc.perform(get("/api/notifications").header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("FOLLOWER_UPDATE"))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/notifications/{id}/read — error: notification not found
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read — 404 when notification not found")
    void markAsRead_notFound_returns404() throws Exception {
        when(notificationService.markAsRead(eq(USER_ID), eq(NOTIF_ID)))
                .thenThrow(new ResourceNotFoundException("Notification not found: " + NOTIF_ID));

        mockMvc.perform(patch("/api/notifications/{id}/read", NOTIF_ID)
                        .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Notification not found: " + NOTIF_ID));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/notifications/{id}/read — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read — 200 OK when notification marked as read")
    void markAsRead_happyPath_returns200() throws Exception {
        NotificationResponse response = NotificationResponse.builder()
                .id(NOTIF_ID).recipientId(USER_ID)
                .type(NotificationType.FOLLOWER_UPDATE).channel(Channel.IN_APP)
                .payload("{}").read(true).createdAt(LocalDateTime.now()).build();

        when(notificationService.markAsRead(USER_ID, NOTIF_ID)).thenReturn(response);

        mockMvc.perform(patch("/api/notifications/{id}/read", NOTIF_ID)
                        .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    // -----------------------------------------------------------------------
    // POST /api/notifications/read-all — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/notifications/read-all — 204 No Content")
    void markAllAsRead_returns204() throws Exception {
        mockMvc.perform(post("/api/notifications/read-all")
                        .header("X-User-Id", USER_ID.toString()))
                .andExpect(status().isNoContent());
    }
}
