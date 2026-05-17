package com.sportlink.search.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_index")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchIndex {

    @Id
    @Column(name = "index_id")
    private String indexId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private EntityType entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "display_name")
    private String displayName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "search_index_keywords",
            joinColumns = @JoinColumn(name = "index_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @Column(name = "last_indexed_at")
    private LocalDateTime lastIndexedAt;

    @Embedded
    private SearchFilters filters;

    public SearchIndex(String indexId) {
        this.indexId = indexId;
    }
}
