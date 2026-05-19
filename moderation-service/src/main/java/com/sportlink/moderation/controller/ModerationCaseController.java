package com.sportlink.moderation.controller;

import com.sportlink.moderation.dto.request.CreateCaseRequest;
import com.sportlink.moderation.dto.request.SubmitVerdictRequest;
import com.sportlink.moderation.dto.response.ModerationCaseResponse;
import com.sportlink.moderation.model.enums.ContentType;
import com.sportlink.moderation.model.enums.Verdict;
import com.sportlink.moderation.service.ModerationCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class ModerationCaseController {

    private final ModerationCaseService caseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModerationCaseResponse createCase(@Valid @RequestBody CreateCaseRequest request) {
        return caseService.createCase(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('MODERATOR')")
    public Page<ModerationCaseResponse> listCases(
            @RequestParam(required = false) ContentType contentType,
            @RequestParam(required = false) Verdict verdict,
            Pageable pageable) {
        return caseService.listCases(contentType, verdict, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR')")
    public ModerationCaseResponse getCase(@PathVariable("id") UUID id) {
        return caseService.getCaseById(id);
    }

    @PutMapping("/{id}/verdict")
    @PreAuthorize("hasRole('MODERATOR')")
    public ModerationCaseResponse submitVerdict(@PathVariable("id") UUID id,
                                                @Valid @RequestBody SubmitVerdictRequest request) {
        return caseService.submitVerdict(id, request);
    }
}
