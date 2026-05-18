package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.TicketCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTicketCommentRepository extends JpaRepository<TicketCommentEntity, UUID> {

    List<TicketCommentEntity> findByTicketIdAndDeletedFalseOrderByCreatedAtAsc(UUID ticketId);

    Optional<TicketCommentEntity> findByIdAndDeletedFalse(UUID id);
}
