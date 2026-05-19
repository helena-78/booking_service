package com.sportlink.moderation.controller;

import com.sportlink.moderation.dto.request.ApplySanctionRequest;
import com.sportlink.moderation.dto.response.AccountSanctionResponse;
import com.sportlink.moderation.service.SanctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sanctions")
@RequiredArgsConstructor
public class SanctionController {

    private final SanctionService sanctionService;

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountSanctionResponse applySanction(@Valid @RequestBody ApplySanctionRequest request) {
        return sanctionService.applySanction(request);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('MODERATOR')")
    public List<AccountSanctionResponse> getSanctionHistory(@PathVariable("userId") UUID userId) {
        return sanctionService.getSanctionHistory(userId);
    }
}
