package com.sportlink.interactionservice.controller;

import com.sportlink.interactionservice.dto.request.CommentRequest;
import com.sportlink.interactionservice.dto.response.CommentResponse;
import com.sportlink.interactionservice.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Post, retrieve, edit and delete comments on activities")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Post a comment on an activity")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/activities/{activityId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse postComment(
            @Parameter(description = "Authenticated user's UUID", required = true)
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID activityId,
            @Valid @RequestBody CommentRequest request) {
        return commentService.postComment(currentUserId, activityId, request);
    }

    @Operation(summary = "Get all comments for an activity",
            description = "Returns comments ordered by creation time (oldest first). Use ?page=0&size=20.")
    @ApiResponse(responseCode = "200", description = "Paginated list of comments")
    @GetMapping("/activities/{activityId}/comments")
    public Page<CommentResponse> getComments(
            @PathVariable UUID activityId,
            @PageableDefault(size = 20) Pageable pageable) {
        return commentService.getComments(activityId, pageable);
    }

    @Operation(summary = "Edit a comment", description = "Only the comment's author can edit it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated"),
            @ApiResponse(responseCode = "403", description = "Caller is not the comment author"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PatchMapping("/comments/{commentId}")
    public CommentResponse editComment(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequest request) {
        return commentService.editComment(currentUserId, commentId, request);
    }

    @Operation(summary = "Delete a comment",
            description = "Allowed for the comment's author or any user with role MODERATOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String currentUserRole,
            @PathVariable UUID commentId) {
        commentService.deleteComment(currentUserId, currentUserRole, commentId);
    }
}
