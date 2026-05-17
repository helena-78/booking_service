package com.sportlink.search.dto;

import java.util.ArrayList;
import java.util.List;

import com.sportlink.search.model.EntityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/search/index.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexEntityRequest {
    private EntityType entityType;
    private String entityId;
    private String displayName;
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    private SearchFiltersDto filters;
}
