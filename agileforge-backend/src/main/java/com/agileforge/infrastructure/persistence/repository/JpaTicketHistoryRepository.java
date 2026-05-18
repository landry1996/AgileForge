package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.TicketHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaTicketHistoryRepository extends JpaRepository<TicketHistoryEntity, UUID> {

    List<TicketHistoryEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}
