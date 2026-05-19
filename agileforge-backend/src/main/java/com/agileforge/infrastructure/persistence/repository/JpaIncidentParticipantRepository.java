package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.IncidentParticipantEntity;
import com.agileforge.infrastructure.persistence.entity.IncidentParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaIncidentParticipantRepository extends JpaRepository<IncidentParticipantEntity, IncidentParticipantId> {

    List<IncidentParticipantEntity> findByIncidentId(UUID incidentId);
}
