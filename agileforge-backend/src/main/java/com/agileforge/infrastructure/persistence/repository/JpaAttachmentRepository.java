package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAttachmentRepository extends JpaRepository<AttachmentEntity, UUID> {

    List<AttachmentEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}
