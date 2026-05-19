package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.TicketLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaTicketLinkRepository extends JpaRepository<TicketLinkEntity, UUID> {

    List<TicketLinkEntity> findBySourceTicketId(UUID sourceTicketId);

    List<TicketLinkEntity> findByTargetTicketId(UUID targetTicketId);

    List<TicketLinkEntity> findBySourceTicketIdOrTargetTicketId(UUID sourceTicketId, UUID targetTicketId);

    void deleteBySourceTicketIdAndTargetTicketIdAndLinkType(UUID sourceTicketId, UUID targetTicketId, String linkType);

    boolean existsBySourceTicketIdAndTargetTicketIdAndLinkType(UUID sourceTicketId, UUID targetTicketId, String linkType);
}
