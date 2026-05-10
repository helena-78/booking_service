package com.sportlink.account.model;

import com.sportlink.account.model.enums.FacilityStatus;
import com.sportlink.account.model.valueobject.FacilityContactInfo;
import com.sportlink.account.model.valueobject.FacilityLocation;
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
@Table(name = "facility_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "center_id")
    private UUID centerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "sport_types", columnDefinition = "TEXT", nullable = false)
    private String sportTypes;

    @Embedded
    private FacilityContactInfo contactInfo;

    @Embedded
    private FacilityLocation location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FacilityStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = FacilityStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
