package com.sportlink.activitymanagement.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportlink.activitymanagement.model.Activity;


public interface ActivityRepository extends JpaRepository<Activity, UUID> {
}