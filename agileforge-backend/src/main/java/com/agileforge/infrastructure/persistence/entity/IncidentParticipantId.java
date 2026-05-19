package com.agileforge.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class IncidentParticipantId implements Serializable {

    private UUID incidentId;
    private UUID userId;

    public IncidentParticipantId() {}

    public IncidentParticipantId(UUID incidentId, UUID userId) {
        this.incidentId = incidentId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidentParticipantId that = (IncidentParticipantId) o;
        return Objects.equals(incidentId, that.incidentId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incidentId, userId);
    }
}
