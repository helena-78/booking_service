package com.sportlink.moderation.service;

import com.sportlink.moderation.dto.request.CreateCaseRequest;
import com.sportlink.moderation.dto.request.SubmitVerdictRequest;
import com.sportlink.moderation.dto.response.ModerationCaseResponse;
import com.sportlink.moderation.exception.CaseNotFoundException;
import com.sportlink.moderation.exception.InvalidVerdictTransitionException;
import com.sportlink.moderation.model.ModerationCase;
import com.sportlink.moderation.model.enums.ContentType;
import com.sportlink.moderation.model.enums.Verdict;
import com.sportlink.moderation.repository.ModerationCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationCaseService {

    private final ModerationCaseRepository caseRepository;

    @Transactional
    public ModerationCaseResponse createCase(CreateCaseRequest request) {
        ModerationCase moderationCase = ModerationCase.builder()
                .reportedContentId(request.getReportedContentId())
                .contentType(request.getContentType())
                .reportedByUserId(request.getReportedByUserId())
                .targetUserId(request.getTargetUserId())
                .content(request.getContent())
                .verdict(Verdict.PENDING)
                .build();
        ModerationCase saved = caseRepository.save(moderationCase);
        log.info("Moderation case created: {}", saved.getCaseId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ModerationCaseResponse getCaseById(UUID caseId) {
        ModerationCase moderationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case not found: " + caseId));
        return toResponse(moderationCase);
    }

    @Transactional(readOnly = true)
    public Page<ModerationCaseResponse> listCases(ContentType contentType, Verdict verdict, Pageable pageable) {
        Page<ModerationCase> page;
        if (contentType != null && verdict != null) {
            page = caseRepository.findByContentTypeAndVerdict(contentType, verdict, pageable);
        } else if (contentType != null) {
            page = caseRepository.findByContentType(contentType, pageable);
        } else if (verdict != null) {
            page = caseRepository.findByVerdict(verdict, pageable);
        } else {
            page = caseRepository.findAll(pageable);
        }
        return page.map(this::toResponse);
    }

    @Transactional
    public ModerationCaseResponse submitVerdict(UUID caseId, SubmitVerdictRequest request) {
        ModerationCase moderationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case not found: " + caseId));

        if (moderationCase.getVerdict() != Verdict.PENDING) {
            throw new InvalidVerdictTransitionException(
                    "Case " + caseId + " already resolved with verdict " + moderationCase.getVerdict());
        }
        if (request.getVerdict() == Verdict.PENDING) {
            throw new InvalidVerdictTransitionException("Verdict cannot be set back to PENDING");
        }

        moderationCase.setVerdict(request.getVerdict());
        moderationCase.setModeratorId(request.getModeratorId());
        moderationCase.setResolvedAt(LocalDateTime.now());
        ModerationCase saved = caseRepository.save(moderationCase);
        log.info("Verdict {} submitted for case {} by moderator {}",
                saved.getVerdict(), saved.getCaseId(), saved.getModeratorId());
        return toResponse(saved);
    }

    private ModerationCaseResponse toResponse(ModerationCase entity) {
        return ModerationCaseResponse.builder()
                .caseId(entity.getCaseId())
                .reportedContentId(entity.getReportedContentId())
                .contentType(entity.getContentType())
                .reportedByUserId(entity.getReportedByUserId())
                .targetUserId(entity.getTargetUserId())
                .content(entity.getContent())
                .verdict(entity.getVerdict())
                .moderatorId(entity.getModeratorId())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
