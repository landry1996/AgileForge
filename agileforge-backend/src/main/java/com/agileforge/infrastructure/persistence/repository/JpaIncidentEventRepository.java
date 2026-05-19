package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.IncidentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaIncidentEventRepository extends JpaRepository<IncidentEventEntity, UUID> {

    List<IncidentEventEntity> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
