package com.sportlink.ratingservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sportlink.ratingservice.model.UserReputation;

@Repository
public interface UserReputationRepository extends JpaRepository<UserReputation, UUID> {
}
