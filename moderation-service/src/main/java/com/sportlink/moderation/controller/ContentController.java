package com.sportlink.moderation.controller;

import com.sportlink.moderation.dto.response.ContentRemovalResponse;
import com.sportlink.moderation.model.enums.ContentType;
import com.sportlink.moderation.service.ContentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentRemovalService contentRemovalService;

    @DeleteMapping("/{contentType}/{contentId}")
    public ContentRemovalResponse removeContent(@PathVariable("contentType") ContentType contentType,
                                                @PathVariable("contentId") UUID contentId) {
        return contentRemovalService.delegateRemoval(contentType, contentId);
    }
}
