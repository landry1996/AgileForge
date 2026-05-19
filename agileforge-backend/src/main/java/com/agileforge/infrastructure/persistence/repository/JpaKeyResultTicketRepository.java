package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.KeyResultTicketEntity;
import com.agileforge.infrastructure.persistence.entity.KeyResultTicketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaKeyResultTicketRepository extends JpaRepository<KeyResultTicketEntity, KeyResultTicketId> {

    void deleteById(KeyResultTicketId id);
}
