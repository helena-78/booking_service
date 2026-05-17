package com.sportlink.moderation.repository;

import com.sportlink.moderation.model.ModerationCase;
import com.sportlink.moderation.model.enums.ContentType;
import com.sportlink.moderation.model.enums.Verdict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModerationCaseRepository extends JpaRepository<ModerationCase, UUID> {

    Page<ModerationCase> findByContentType(ContentType contentType, Pageable pageable);

    Page<ModerationCase> findByVerdict(Verdict verdict, Pageable pageable);

    Page<ModerationCase> findByContentTypeAndVerdict(ContentType contentType, Verdict verdict, Pageable pageable);
}
