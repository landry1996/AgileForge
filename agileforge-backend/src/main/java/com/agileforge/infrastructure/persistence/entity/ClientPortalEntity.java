package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "client_portals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientPortalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "project_id", nullable = false, unique = true)
    private UUID projectId;

    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "welcome_message", columnDefinition = "TEXT")
    private String welcomeMessage;

    @Column(name = "allowed_ticket_types", length = 500)
    private String allowedTicketTypes;

    @Column(name = "show_roadmap", nullable = false)
    private boolean showRoadmap = true;

    @Column(name = "show_releases", nullable = false)
    private boolean showReleases = true;

    @Column(name = "show_changelog", nullable = false)
    private boolean showChangelog = true;

    @Column(name = "custom_branding", columnDefinition = "JSONB")
    private String customBranding;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
