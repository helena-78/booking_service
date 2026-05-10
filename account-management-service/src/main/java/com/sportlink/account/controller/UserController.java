package com.sportlink.account.controller;

import com.sportlink.account.dto.request.RegisterUserRequest;
import com.sportlink.account.dto.request.UpdateUserRequest;
import com.sportlink.account.dto.request.UpdateUserStatusRequest;
import com.sportlink.account.dto.response.UserProfileResponse;
import com.sportlink.account.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse registerUser(@Valid @RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping("/{id}")
    public UserProfileResponse getUser(@PathVariable("id") UUID id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserProfileResponse updateUser(@PathVariable("id") UUID id,
                                          @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @PutMapping("/{id}/status")
    public UserProfileResponse updateUserStatus(@PathVariable("id") UUID id,
                                                @Valid @RequestBody UpdateUserStatusRequest request) {
        return userService.updateUserStatus(id, request);
    }
}
