package com.sportlink.account.repository;

import com.sportlink.account.model.FacilityProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacilityProfileRepository extends JpaRepository<FacilityProfile, UUID> {

    Optional<FacilityProfile> findByContactInfoEmail(String email);

    boolean existsByContactInfoEmail(String email);
}
