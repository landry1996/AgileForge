package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.DocumentTicketLinkEntity;
import com.agileforge.infrastructure.persistence.entity.DocumentTicketLinkId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaDocumentTicketLinkRepository extends JpaRepository<DocumentTicketLinkEntity, DocumentTicketLinkId> {

    @Query("SELECT dtl.id.ticketId FROM DocumentTicketLinkEntity dtl WHERE dtl.id.documentId = :documentId")
    List<UUID> findTicketIdsByDocumentId(@Param("documentId") UUID documentId);

    void deleteById(DocumentTicketLinkId id);
}
