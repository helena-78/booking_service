package com.sportlink.interactionservice.controller;

import com.sportlink.interactionservice.dto.request.FollowRequest;
import com.sportlink.interactionservice.dto.response.FollowResponse;
import com.sportlink.interactionservice.dto.response.UserSummaryResponse;
import com.sportlink.interactionservice.service.FollowService;
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
@Tag(name = "Follows", description = "Follow / unfollow users and browse the social graph")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "Follow a user",
            description = "Creates a follow relationship. The authenticated user (X-User-Id) follows targetUserId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Follow created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or self-follow attempt"),
            @ApiResponse(responseCode = "404", description = "Target user not found"),
            @ApiResponse(responseCode = "409", description = "Already following this user")
    })
    @PostMapping("/follows")
    @ResponseStatus(HttpStatus.CREATED)
    public FollowResponse followUser(
            @Parameter(description = "Authenticated user's UUID", required = true)
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody FollowRequest request) {
        return followService.followUser(currentUserId, request.getTargetUserId());
    }

    @Operation(summary = "Unfollow a user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unfollowed successfully"),
            @ApiResponse(responseCode = "404", description = "Follow relationship not found")
    })
    @DeleteMapping("/follows/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowUser(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID targetUserId) {
        followService.unfollowUser(currentUserId, targetUserId);
    }

    @Operation(summary = "List followers of a user",
            description = "Returns a paginated list of users who follow the specified user. Use ?page=0&size=20.")
    @ApiResponse(responseCode = "200", description = "Paginated list of followers")
    @GetMapping("/users/{userId}/followers")
    public Page<UserSummaryResponse> getFollowers(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return followService.getFollowers(userId, pageable);
    }

    @Operation(summary = "List users that a user is following",
            description = "Returns a paginated list of users that the specified user follows. Use ?page=0&size=20.")
    @ApiResponse(responseCode = "200", description = "Paginated list of following")
    @GetMapping("/users/{userId}/following")
    public Page<UserSummaryResponse> getFollowing(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return followService.getFollowing(userId, pageable);
    }
}
