package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_participants")
@IdClass(IncidentParticipantId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentParticipantEntity {

    @Id
    @Column(name = "incident_id", nullable = false)
    private UUID incidentId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
}
