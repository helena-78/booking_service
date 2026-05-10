package com.sportlink.account.model;

import com.sportlink.account.model.enums.SkillLevel;
import com.sportlink.account.model.enums.UserRole;
import com.sportlink.account.model.enums.UserStatus;
import com.sportlink.account.model.valueobject.UserContactInfo;
import com.sportlink.account.model.valueobject.UserLabel;
import com.sportlink.account.model.valueobject.UserLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Embedded
    private UserContactInfo contactInfo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level")
    private SkillLevel skillLevel;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "sport_preferences", columnDefinition = "TEXT")
    private String sportPreferences;

    @Embedded
    private UserLocation location;

    @Column(name = "availability_id")
    private UUID availabilityId;

    @Embedded
    private UserLabel label;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.role == null) {
            this.role = UserRole.USER;
        }
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
