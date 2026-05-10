package com.sportlink.interactionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportlink.interactionservice.dto.request.CommentRequest;
import com.sportlink.interactionservice.dto.response.CommentResponse;
import com.sportlink.interactionservice.exception.ForbiddenException;
import com.sportlink.interactionservice.exception.GlobalExceptionHandler;
import com.sportlink.interactionservice.exception.ResourceNotFoundException;
import com.sportlink.interactionservice.service.CommentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for CommentController using @WebMvcTest.
 *
 * CommentService depends on CommentRepository (persistence) and RabbitTemplate
 * (async event publishing to Notification Service via RabbitMQ). Both are
 * bypassed here by mocking CommentService, letting us verify controller
 * behaviour in isolation.
 */
@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private static final UUID AUTHOR_ID   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER  = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private static final UUID ACTIVITY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID COMMENT_ID  = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    // -----------------------------------------------------------------------
    // POST /api/activities/{activityId}/comments — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/activities/{id}/comments — 201 Created with returned comment")
    void postComment_happyPath_returns201() throws Exception {
        CommentResponse response = CommentResponse.builder()
                .id(COMMENT_ID)
                .activityId(ACTIVITY_ID)
                .authorId(AUTHOR_ID)
                .body("Great activity!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(commentService.postComment(eq(AUTHOR_ID), eq(ACTIVITY_ID), any(CommentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/activities/{id}/comments", ACTIVITY_ID)
                        .header("X-User-Id", AUTHOR_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequest("Great activity!", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(COMMENT_ID.toString()))
                .andExpect(jsonPath("$.body").value("Great activity!"))
                .andExpect(jsonPath("$.authorId").value(AUTHOR_ID.toString()));
    }

    // -----------------------------------------------------------------------
    // POST — validation error
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/activities/{id}/comments — 400 when body is blank")
    void postComment_blankBody_returns400() throws Exception {
        mockMvc.perform(post("/api/activities/{id}/comments", ACTIVITY_ID)
                        .header("X-User-Id", AUTHOR_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequest("", null))))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // GET /api/activities/{activityId}/comments — happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/activities/{id}/comments — 200 with paginated comment list")
    void getComments_returnsPage() throws Exception {
        CommentResponse c = CommentResponse.builder()
                .id(COMMENT_ID).activityId(ACTIVITY_ID).authorId(AUTHOR_ID)
                .body("Count me in!").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(commentService.getComments(eq(ACTIVITY_ID), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(c)));

        mockMvc.perform(get("/api/activities/{id}/comments", ACTIVITY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].body").value("Count me in!"));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/comments/{commentId} — error: non-author tries to edit
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/comments/{id} — 403 Forbidden when caller is not the comment author")
    void editComment_notAuthor_returns403() throws Exception {
        when(commentService.editComment(eq(OTHER_USER), eq(COMMENT_ID), any(CommentRequest.class)))
                .thenThrow(new ForbiddenException("Only the comment author can edit this comment"));

        mockMvc.perform(patch("/api/comments/{id}", COMMENT_ID)
                        .header("X-User-Id", OTHER_USER.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentRequest("Edited text", null))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only the comment author can edit this comment"));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/comments/{commentId} — error: comment not found
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/comments/{id} — 404 when comment does not exist")
    void deleteComment_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Comment not found: " + COMMENT_ID))
                .when(commentService).deleteComment(any(UUID.class), any(String.class), eq(COMMENT_ID));

        mockMvc.perform(delete("/api/comments/{id}", COMMENT_ID)
                        .header("X-User-Id", AUTHOR_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Comment not found: " + COMMENT_ID));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/comments/{commentId} — happy path (moderator deletes)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/comments/{id} — 204 when moderator deletes another user's comment")
    void deleteComment_byModerator_returns204() throws Exception {
        mockMvc.perform(delete("/api/comments/{id}", COMMENT_ID)
                        .header("X-User-Id", OTHER_USER.toString())
                        .header("X-User-Role", "MODERATOR"))
                .andExpect(status().isNoContent());
    }
}
