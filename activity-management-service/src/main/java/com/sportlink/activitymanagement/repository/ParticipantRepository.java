package com.sportlink.activitymanagement.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportlink.activitymanagement.model.Participant;


public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    Optional<Participant> findByActivity_ActivityIdAndUserId(UUID activityId, UUID userId);
}