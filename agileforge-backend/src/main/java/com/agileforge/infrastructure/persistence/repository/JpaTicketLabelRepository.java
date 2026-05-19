package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.TicketLabelEntity;
import com.agileforge.infrastructure.persistence.entity.TicketLabelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaTicketLabelRepository extends JpaRepository<TicketLabelEntity, TicketLabelId> {

    List<TicketLabelEntity> findByTicketId(UUID ticketId);

    void deleteByTicketIdAndLabelId(UUID ticketId, UUID labelId);
}
