package com.sportlink.search.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.search.model.EntityType;
import com.sportlink.search.model.SearchIndex;

@Repository
public interface SearchIndexRepository extends CrudRepository<SearchIndex, String> {

    List<SearchIndex> findByEntityType(EntityType entityType);

    Optional<SearchIndex> findByEntityTypeAndEntityId(EntityType entityType, String entityId);

    @Transactional
    void deleteByEntityTypeAndEntityId(EntityType entityType, String entityId);

    boolean existsByEntityTypeAndEntityId(EntityType entityType, String entityId);
}
