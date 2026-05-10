package com.sportlink.account.repository;

import com.sportlink.account.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByContactInfoEmail(String email);

    boolean existsByContactInfoEmail(String email);
}
