package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.ReleaseTicketEntity;
import com.agileforge.infrastructure.persistence.entity.ReleaseTicketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaReleaseTicketRepository extends JpaRepository<ReleaseTicketEntity, ReleaseTicketId> {

    @Query("SELECT rt.id.ticketId FROM ReleaseTicketEntity rt WHERE rt.id.releaseId = :releaseId")
    List<UUID> findTicketIdsByReleaseId(@Param("releaseId") UUID releaseId);

    void deleteById(ReleaseTicketId id);
}
