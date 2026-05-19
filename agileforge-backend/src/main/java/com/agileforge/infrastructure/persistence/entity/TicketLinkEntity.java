package com.agileforge.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_ticket_id", nullable = false)
    private UUID sourceTicketId;

    @Column(name = "target_ticket_id", nullable = false)
    private UUID targetTicketId;

    @Column(name = "link_type", nullable = false, length = 30)
    private String linkType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;
}
